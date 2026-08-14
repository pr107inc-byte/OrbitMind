package com.tk854.localmind.data.mapper

import com.tk854.localmind.data.local.entity.MessageEntity
import com.tk854.localmind.domain.model.Message
import com.tk854.localmind.domain.model.MessageRole

fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        conversationId = conversationId,
        role = MessageRole.fromApiString(role),
        content = content,
        timestamp = timestamp,
        tokenCount = tokenCount,
        imageUri = imageUri,
        reasoningContent = reasoningContent,
        inferenceSource = inferenceSource,
        ttftMs = ttftMs,
        generationMs = generationMs,
        tokensPerSecond = tokensPerSecond,
        thinkingDurationSec = thinkingDurationSec
    )
}

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        conversationId = conversationId,
        role = role.toApiString(),
        content = content,
        timestamp = timestamp,
        tokenCount = tokenCount,
        imageUri = imageUri,
        reasoningContent = reasoningContent,
        inferenceSource = inferenceSource,
        ttftMs = ttftMs,
        generationMs = generationMs,
        tokensPerSecond = tokensPerSecond,
        thinkingDurationSec = thinkingDurationSec
    )
}
