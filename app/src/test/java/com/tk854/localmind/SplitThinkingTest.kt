package com.tk854.localmind

data class ThinkingTagPair(val startTag: String, val endTag: String)
data class ThinkingTagMatch(val startIndex: Int, val startTag: String, val endTag: String)

val THINKING_TAG_PAIRS = listOf(
    ThinkingTagPair("<think>", "</think>"),
    ThinkingTagPair("<thinking>", "</thinking>"),
    ThinkingTagPair("<thought>", "</thought>"),
    ThinkingTagPair("<reasoning>", "</reasoning>"),
    ThinkingTagPair("<|start_thinking|>", "<|end_thinking|>")
)

fun splitThinkingContent(fullText: String): Pair<String, String> {
    if (fullText.isBlank()) return Pair("", "")

    val reasoning = StringBuilder()
    var main = fullText

    while (true) {
        val nextMatch = THINKING_TAG_PAIRS
            .mapNotNull { pair ->
                val startIndex = main.indexOf(pair.startTag, ignoreCase = true)
                if (startIndex == -1) null else ThinkingTagMatch(startIndex, pair.startTag, pair.endTag)
            }
            .minByOrNull { it.startIndex } ?: break

        val contentStart = nextMatch.startIndex + nextMatch.startTag.length
        val contentEnd = main.indexOf(nextMatch.endTag, contentStart, ignoreCase = true)
        val extractedReasoning = if (contentEnd == -1) {
            main.substring(contentStart)
        } else {
            main.substring(contentStart, contentEnd)
        }

        if (extractedReasoning.isNotBlank()) {
            if (reasoning.isNotEmpty()) reasoning.append('\n')
            reasoning.append(extractedReasoning.trim())
        }

        main = if (contentEnd == -1) {
            main.substring(0, nextMatch.startIndex)
        } else {
            main.removeRange(nextMatch.startIndex, contentEnd + nextMatch.endTag.length)
        }
    }

    return Pair(reasoning.toString().trim(), main.trim())
}

fun main() {
    val fullText = "<think>\nHere is reasoning\n</think>\nAnd here is the answer."
    val (think, mainStr) = splitThinkingContent(fullText)
    println("Think: $think")
    println("Main: $mainStr")
}
