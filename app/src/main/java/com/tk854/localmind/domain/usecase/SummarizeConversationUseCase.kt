package com.tk854.localmind.domain.usecase

import android.util.Log
import com.tk854.localmind.data.repository.ChatRepository
import com.tk854.localmind.llm.GenerationResult
import com.tk854.localmind.llm.InferenceConfig
import com.tk854.localmind.domain.model.MessageRole
import com.tk854.localmind.llm.ModelLifecycleManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SummarizeConversationUseCase @Inject constructor(
    private val chatRepository: ChatRepository,
    private val modelLifecycleManager: ModelLifecycleManager,
    private val promptSanitizationUseCase: PromptSanitizationUseCase
) {
    suspend operator fun invoke(conversationId: String) {
        withContext(Dispatchers.IO) {
            try {
                val conversation = chatRepository.getConversationById(conversationId) ?: return@withContext
                val allMessages = chatRepository.getMessagesByConversationSync(conversationId)

                // Guard: only summarize when there are enough messages AND engine is free.
                // Avoids competing with active chat generation.
                if (allMessages.size < 5) return@withContext
                if (chatRepository.isGenerating()) return@withContext

                // Increase frequency: summarize every time history grows to keep it fresh
                // But skip if it's already summarized recently (e.g. within last 1 message) to save battery
                // However, the user wants "fast", so we prioritize frequent small summaries.

                val previousSummaryText = conversation.summary?.let { "Previous Summary: $it\n" } ?: ""

                // Summarize ALL messages to create a complete global summary of the conversation
                val messagesToSummarize = allMessages
                val archiveText = messagesToSummarize.takeLast(15).joinToString("\n") {
                    val roleName = if (it.role == MessageRole.USER) "User" else "Model"
                    "$roleName: ${it.content}"
                }

                val prompt = "Update the following chat summary by incorporating the new archived messages. Keep it very concise and factual. Respond with ONLY the updated summary.\n\n$previousSummaryText New Archived Messages:\n$archiveText\n\nUpdated Summary:"

                val tunedConfig = modelLifecycleManager.tuneInferenceConfig(
                    InferenceConfig(maxTokens = 256, temperature = 0.2f, contextSize = 2048)
                )

                val responseBuilder = StringBuilder()
                chatRepository.generateResponse(
                    prompt = prompt,
                    config = tunedConfig,
                    shouldUpdateCache = false
                ).collect { result ->
                    if (result is GenerationResult.Token) {
                        responseBuilder.append(promptSanitizationUseCase.stripStreamingTags(result.text))
                    }
                }

                val newSummary = promptSanitizationUseCase.sanitizeAssistantReply(responseBuilder.toString(), tunedConfig.stopTokens)
                if (newSummary.isNotBlank() && !newSummary.contains(prompt.takeLast(20))) {
                    chatRepository.updateConversationSummary(conversationId, newSummary)
                }
            } catch (e: Exception) {
                Log.e("Summarizer", "Auto-summarization failed", e)
            }
        }
    }
}
