package com.tk854.localmind.llm

data class ActivationOptions(
    val forceLoad: Boolean = false,
    val source: ActivationSource = ActivationSource.USER
)

enum class ActivationSource {
    USER,
    USER_SWITCH,   // Chat screen se model change kiya
    AUTO_RESTORE,
    CHAT_AUTOSTART,
    BENCHMARK,
    INTERNAL
}
