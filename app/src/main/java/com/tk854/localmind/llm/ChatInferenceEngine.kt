package com.tk854.localmind.llm

import com.tk854.localmind.domain.model.InferenceSource
import kotlinx.coroutines.flow.Flow

interface ChatInferenceEngine {
    val source: InferenceSource
    fun generate(
        prompt: String,
        config: InferenceConfig,
        shouldUpdateCache: Boolean = true,
        remoteModelOverride: String? = null
    ): Flow<GenerationResult>
}
