package com.tk854.localmind.llm

import com.tk854.localmind.domain.model.InferenceSource

data class InferenceTelemetry(
    val source: InferenceSource,
    val ttftMs: Long? = null,
    val totalTimeMs: Long = 0L,
    val tokensGenerated: Int = 0,
    val tokensPerSecond: Float = 0f
)
