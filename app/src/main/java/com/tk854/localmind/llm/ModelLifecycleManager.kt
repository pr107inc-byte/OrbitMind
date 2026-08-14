package com.tk854.localmind.llm

import android.content.Context
import android.app.ActivityManager
import com.tk854.localmind.core.performance.DeviceProfileManager
import com.tk854.localmind.data.repository.ChatRepository
import com.tk854.localmind.data.repository.ModelRepository
import com.tk854.localmind.data.repository.SettingsRepository
import com.tk854.localmind.domain.model.Model
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import java.io.File

data class ModelLifecyclePlan(
    val contextSize: Int,
    val threadCount: Int,
    val maxTokens: Int
)

@Singleton
class ModelLifecycleManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val modelRepository: ModelRepository,
    private val chatRepository: ChatRepository,
    private val deviceProfileManager: DeviceProfileManager,
    private val settingsRepository: SettingsRepository
) {
    private val modelLock = Mutex()
    private var pendingModelId: String? = null

    suspend fun activateModelSafely(
        modelId: String,
        options: ActivationOptions = ActivationOptions()
    ): Result<ModelLifecyclePlan> {
        pendingModelId = modelId

        // TTFT FIX: Fast-path check BEFORE acquiring modelLock.
        // Agar model already loaded hai toh lock acquire karne ki zarurat hi nahi.
        // Pehle lock ke andar yeh check tha â€” lock wait + 3 DataStore reads = ~8s delay.
        if (!options.forceLoad) {
            val model = modelRepository.getModelById(modelId)
            if (model != null && modelRepository.modelFileExists(model)) {
                val (loadedKey, loadedContext) = chatRepository.getLoadedModelDetails()
                val targetKey = if (model.storageType == com.tk854.localmind.core.storage.ModelStorageType.SAF) {
                    "saf:${model.storageUri}"
                } else {
                    val normalizedPath = model.filePath
                        ?.takeIf { it.isNotBlank() }
                        ?.let { File(it).absolutePath }
                        ?: model.fileName
                    "path:$normalizedPath"
                }
                if (loadedKey == targetKey && chatRepository.isModelLoaded()) {
                    // Already-loaded fast path still has to update DB/UI active state.
                    modelRepository.activateModel(modelId)
                    settingsRepository.clearActiveCloudModel()
                    // Already loaded â€” return immediately, zero disk I/O
                    return Result.success(ModelLifecyclePlan(
                        contextSize = loadedContext,
                        threadCount = 0, // not used by caller
                        maxTokens = 0   // not used by caller
                    ))
                }
            }
        }

        return modelLock.withLock {
            // Check if another request has superseded this one while waiting for the lock
            if (pendingModelId != modelId) {
                return Result.failure(CancellationException("Superseded by a newer activation request"))
            }

            val model = modelRepository.getModelById(modelId)
                ?: return Result.failure(IllegalArgumentException("Model not found"))

            if (!modelRepository.modelFileExists(model)) {
                return Result.failure(IllegalStateException("Model file is missing. Re-download required."))
            }

            val userContextSize = settingsRepository.contextSize.first()
            val requestedContextSize = userContextSize

            // Check if this exact model configuration is already loaded
            val (loadedKey, loadedContext) = chatRepository.getLoadedModelDetails()
            val targetKey = if (model.storageType == com.tk854.localmind.core.storage.ModelStorageType.SAF) {
                "saf:${model.storageUri}"
            } else {
                val normalizedPath = model.filePath
                    ?.takeIf { it.isNotBlank() }
                    ?.let { File(it).absolutePath }
                    ?: model.fileName
                "path:$normalizedPath"
            }

            if (loadedKey == targetKey && !options.forceLoad) {
                // Already loaded with correct config, skip reload
                val currentPlan = ModelLifecyclePlan(
                    contextSize = loadedContext,
                    threadCount = settingsRepository.threadCount.first(),
                    maxTokens = settingsRepository.maxTokens.first()
                )
                // Just ensure it is marked active
                modelRepository.activateModel(modelId)
                settingsRepository.clearActiveCloudModel()
                return Result.success(currentPlan)
            }

            // BUG FIX: Profile cache invalidate karo taaki fresh RAM reading mile.
            // Pehla model unload ho chuka hai â€” cached value stale hai.
            deviceProfileManager.invalidateProfileCache()

            val requestedPlan = if (options.forceLoad) {
                deviceProfileManager.forcePlan(
                    modelSizeBytes = model.sizeBytes,
                    modelNameHint = model.name,
                    quantizationHint = model.quantization,
                    parameterCountHint = model.parameterCount,
                    requestedContextSize = requestedContextSize
                )
            } else {
                deviceProfileManager.safePlan(
                    modelSizeBytes = model.sizeBytes,
                    modelNameHint = model.name,
                    quantizationHint = model.quantization,
                    parameterCountHint = model.parameterCount,
                    requestedContextSize = requestedContextSize
                )
            }

            val fallbackForcePlan = if (!requestedPlan.allowed) {
                deviceProfileManager.forcePlan(
                    modelSizeBytes = model.sizeBytes,
                    modelNameHint = model.name,
                    quantizationHint = model.quantization,
                    parameterCountHint = model.parameterCount,
                    requestedContextSize = requestedContextSize
                )
            } else {
                null
            }

            val effectivePlan = when {
                requestedPlan.allowed -> requestedPlan
                fallbackForcePlan?.allowed == true -> fallbackForcePlan
                else -> requestedPlan
            }

            val plan = if (effectivePlan.allowed) {
                ModelLifecyclePlan(
                    contextSize = effectivePlan.contextSize,
                    threadCount = effectivePlan.threadCount,
                    maxTokens = effectivePlan.maxTokens
                )
            } else {
                val totalRamGb = deviceProfileManager.currentProfile().totalRamGb
                ModelLifecyclePlan(
                    contextSize = model.contextLength.coerceIn(512, if (totalRamGb <= 3) 1024 else 2048),
                    // PERF FIX: 4 threads minimum â€” 2 threads was halving speed on 8-core SoCs
                    threadCount = 4,
                    maxTokens = if (totalRamGb <= 3) 768 else 1024
                )
            }

            // POCKETPAL FIX: Dusra model load se pehle pehla ZAROOR unload karo.
            // PocketPal: _releaseContextInternal() â†’ ctx.release() â†’ NativeHardwareInfo.getAvailableMemory()
            // LocalMind: Same approach â€” unload + verify RAM freed before next load.
            //
            // ROOT CAUSE of "1B model not loading" bug:
            // SmolLM2-1.7B loaded (1.7GB in RAM). Llama-1B needs 1.23GB.
            // Total = 2.93GB but only 2.0GB available â†’ CRASH.
            // Simple fix: unload pehle wala, VERIFY RAM freed, THEN load naya.
            if (chatRepository.isModelLoaded()) {
                chatRepository.stopGeneration()
                withTimeoutOrNull(8_000L) {
                    chatRepository.unloadModel()
                }
                // POCKETPAL FIX: PocketPal release ke baad availableMemoryCeiling update karta hai.
                // Hum bhi GC + actual RAM measurement karte hain.
                System.gc()
                System.runFinalization()

                // POCKETPAL EXACT: Wait for OS to actually reclaim native pages.
                // PocketPal: "await new Promise(resolve => setTimeout(resolve, 100))"
                // Hum 600ms wait karte hain â€” Android mein native heap reclaim
                // JS se slower hota hai (JNI deallocation + OS page reclaim).
                kotlinx.coroutines.delay(600L)

                // POCKETPAL EXACT: Release ke baad fresh RAM measure karo.
                // PocketPal: NativeHardwareInfo.getAvailableMemory() after ctx.release()
                // Agar abhi bhi RAM low hai toh aur wait karo (max 3 attempts).
                val actMgr = context.getSystemService(Context.ACTIVITY_SERVICE)
                    as ActivityManager
                var verifyAttempts = 0
                val totalRamBytes = run {
                    val mi = ActivityManager.MemoryInfo()
                    actMgr.getMemoryInfo(mi)
                    mi.totalMem
                }
                while (verifyAttempts < 3) {
                    val memInfo = ActivityManager.MemoryInfo()
                    actMgr.getMemoryInfo(memInfo)
                    // Available RAM > 40% of total = safe to load next model
                    if (memInfo.availMem > totalRamBytes * 0.40) break
                    android.util.Log.w("ModelLifecycle",
                        "RAM not yet freed (${memInfo.availMem / 1048576}MB avail), waiting... attempt ${verifyAttempts + 1}")
                    System.gc()
                    System.runFinalization()
                    kotlinx.coroutines.delay(500L)
                    verifyAttempts++
                }
            }

            try {
                val loadResult = withTimeout(95_000L) {
                    chatRepository.loadModel(model)
                }
                if (loadResult.isFailure) {
                    Result.failure(
                        loadResult.exceptionOrNull() ?: IllegalStateException("Failed to load selected model")
                    )
                } else {
                    modelRepository.activateModel(modelId)
                    settingsRepository.clearActiveCloudModel()
                    // TTFT FIX: Result.success pehle return karo, settings baad mein apply hongi.
                    // modelLock.withLock ke andar hi hain isliye withLock block se bahar nahi ja sakte.
                    // Simple fix: directly call karo â€” ye fast hai (sirf agar showAdvanced=false).
                    runCatching { applyRecommendedSamplingSettings(model) }
                    Result.success(plan)
                }
            } catch (timeout: TimeoutCancellationException) {
                Result.failure(IllegalStateException("Model activation timed out"))
            } catch (t: Throwable) {
                Result.failure(t)
            }
        }
    }

    suspend fun ensureModelLoaded(model: Model): Result<ModelLifecyclePlan> {
        if (!modelRepository.modelFileExists(model)) {
            return Result.failure(IllegalStateException("Model file is missing. Re-download required."))
        }

        val check = deviceProfileManager.checkModelLoad(
            modelSizeBytes = model.sizeBytes,
            modelNameHint = model.name,
            quantizationHint = model.quantization,
            parameterCountHint = model.parameterCount,
            requestedContextSize = model.contextLength
        )

        val result = chatRepository.loadModel(model)
        if (result.isFailure) {
            return Result.failure(result.exceptionOrNull() ?: Exception("Failed to load model"))
        }

        val plan = if (check.allowed) {
            ModelLifecyclePlan(
                contextSize = check.tunedContextSize,
                threadCount = check.tunedThreadCount,
                maxTokens = check.tunedMaxTokens
            )
        } else {
            val totalRamGb = deviceProfileManager.currentProfile().totalRamGb
            ModelLifecyclePlan(
                contextSize = model.contextLength.coerceIn(512, 1024),
                threadCount = if (totalRamGb <= 4) 1 else 2,
                maxTokens = if (totalRamGb <= 4) 64 else 128
            )
        }

        return Result.success(
            plan
        )
    }

    suspend fun unloadModelSafely() {
        chatRepository.stopGeneration()
        withTimeout(10_000L) {
            chatRepository.unloadModel()
        }
        modelRepository.clearActiveModel()
    }

    fun tuneInferenceConfig(config: InferenceConfig, benchmarkMode: Boolean = false): InferenceConfig {
        return deviceProfileManager.tuneInferenceConfig(config, benchmarkMode = benchmarkMode)
    }

    private suspend fun applyRecommendedInferenceSettings(
        plan: ModelLifecyclePlan
    ) {
        settingsRepository.updateContextSize(plan.contextSize)
        settingsRepository.updateThreadCount(plan.threadCount)
    }

    private suspend fun applyRecommendedSamplingSettings(
        model: Model
    ) {
        val showAdvanced = settingsRepository.showAdvancedSettings.first()
        if (showAdvanced) {
            return
        }

        settingsRepository.updateTemperature(model.recommendedTemperature.coerceIn(0.1f, 1.2f))
        settingsRepository.updateTopP(model.recommendedTopP.coerceIn(0.5f, 0.99f))
        settingsRepository.updateTopK(model.recommendedTopK.coerceIn(10, 100))
        settingsRepository.updateRepeatPenalty(model.recommendedRepeatPenalty.coerceIn(1.0f, 1.5f))
    }
}
