package com.tk854.localmind.domain.usecase

import com.tk854.localmind.domain.model.Message
import com.tk854.localmind.data.repository.ChatRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetChatMessagesUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(conversationId: String): Flow<List<Message>> {
        return chatRepository.getMessagesByConversation(conversationId)
    }
}
