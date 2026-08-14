package com.tk854.localmind.llm

/**
 * Inference configuration â€” PocketPal completionSettingsVersions.ts v3 ke saath fully synced.
 * Saare parameters same defaults use karte hain jaise PocketPal AI.
 */
data class InferenceConfig(
    // â”€â”€ Core sampling params â”€â”€
    val temperature: Float = 0.7f,          // PocketPal: temperature = 0.7
    val topK: Int = 40,                     // PocketPal: top_k = 40
    val topP: Float = 0.95f,               // PocketPal: top_p = 0.95 (was 0.9)
    val minP: Float = 0.05f,               // PocketPal: min_p = 0.05
    val xtcThreshold: Float = 0.1f,        // PocketPal: xtc_threshold = 0.1
    val xtcProbability: Float = 0.0f,      // PocketPal: xtc_probability = 0.0 (disabled)
    val typicalP: Float = 1.0f,            // PocketPal: typical_p = 1.0 (disabled)
    // â”€â”€ Repetition penalty params â”€â”€
    val repeatPenalty: Float = 1.15f,      // Repetition rokne ke liye 1.15 â€” 1.0 = no penalty
    val penaltyLastN: Int = 256,           // 64 se badha ke 256 â€” bade paragraphs cover honge
    val penaltyFreq: Float = 0.0f,         // PocketPal: penalty_freq = 0.0
    val penaltyPresent: Float = 0.0f,      // PocketPal: penalty_present = 0.0
    // â”€â”€ Mirostat params â”€â”€
    val mirostat: Int = 0,                 // PocketPal: mirostat = 0 (off)
    val mirostatTau: Float = 5.0f,         // PocketPal: mirostat_tau = 5.0
    val mirostatEta: Float = 0.1f,         // PocketPal: mirostat_eta = 0.1
    // â”€â”€ Generation control â”€â”€
    val maxTokens: Int = 1024,             // PocketPal: n_predict = 1024
    val nProbs: Int = 0,                   // PocketPal: n_probs = 0
    val seed: Int = -1,                    // PocketPal: seed = -1 (random)
    // â”€â”€ Template / thinking â”€â”€
    val jinja: Boolean = true,             // PocketPal: jinja = true
    val enableThinking: Boolean = true,    // PocketPal: enable_thinking = true
    val includeThinkingInContext: Boolean = true, // PocketPal: include_thinking_in_context = true
    // â”€â”€ Context / hardware params â”€â”€
    val contextSize: Int = 2048,
    val threadCount: Int = 4,
    val gpuLayers: Int = -1,               // -1 = Auto GPU
    // â”€â”€ Misc â”€â”€
    val stopTokens: List<String> = emptyList(),
    val documentBytes: ByteArray? = null,
    val documentUri: String? = null
) {
    companion object {
        val DEFAULT = InferenceConfig()

        fun forDevice(ramGB: Int): InferenceConfig {
            // PERF FIX: PocketPal formula â€” cores * 0.8 (capped by DeviceProfileManager later).
            // Old code hard-capped ALL devices at 4 threads. An 8-core Snapdragon 8 Gen 3
            // with 12GB RAM was stuck at 4 threads when it should use 6.
            // DeviceProfileManager.tuneInferenceConfig() applies final per-device caps.
            val cores = Runtime.getRuntime().availableProcessors()
            val ppThreads = if (cores > 4) (cores * 0.8).toInt().coerceAtLeast(4) else cores.coerceAtLeast(2)
            return when {
                ramGB <= 3 -> DEFAULT.copy(contextSize = 512,  threadCount = ppThreads.coerceAtMost(4), maxTokens = 512,  topK = 40)
                ramGB <= 4 -> DEFAULT.copy(contextSize = 2048, threadCount = ppThreads.coerceAtMost(6), maxTokens = 1024, topK = 40)
                ramGB <= 6 -> DEFAULT.copy(contextSize = 4096, threadCount = ppThreads.coerceAtMost(6), maxTokens = 2048, topK = 40)
                ramGB >= 12 -> DEFAULT.copy(contextSize = 8192, threadCount = ppThreads, maxTokens = 4096, topK = 48)
                else        -> DEFAULT.copy(contextSize = 4096, threadCount = ppThreads, maxTokens = 2048, topK = 40)
            }
        }
    }
}
