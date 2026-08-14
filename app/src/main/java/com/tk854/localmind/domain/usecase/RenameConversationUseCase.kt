package com.tk854.localmind.domain.usecase

import com.tk854.localmind.data.repository.ChatRepository
import javax.inject.Inject

class RenameConversationUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String, newTitle: String) {
        chatRepository.renameConversation(conversationId, newTitle)
    }
}
