package com.tk854.localmind.domain.usecase

import com.tk854.localmind.data.repository.ChatRepository
import javax.inject.Inject

class DeleteMessageUseCase @Inject constructor(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(messageId: String) {
        chatRepository.deleteMessage(messageId)
    }
}
