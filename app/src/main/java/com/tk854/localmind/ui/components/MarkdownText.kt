package com.tk854.localmind.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tk854.localmind.ui.theme.NeonText

// Streaming ke dauran incomplete code block close karo (non-streaming only)
fun closeIncompleteCodeBlocks(text: String): String {
    var count = 0
    var i = 0
    while (i <= text.length - 3) {
        if (text[i] == '`' && text[i + 1] == '`' && text[i + 2] == '`') { count++; i += 3 } else i++
    }
    return if (count % 2 != 0) "$text\n```" else text
}

// Tags jo reasoning content hain - inhe KABHI strip mat karo
private val REASONING_TAG_NAMES = setOf(
    "think", "thinking", "thought", "reasoning", "analysis"
)

/**
 * Chat template tag filter - sirf pipe-style aur known chat template tags strip karo.
 * Reasoning tags (<think>, <thinking>, etc.) KABHI strip nahi kiye jaate.
 * Answer content mein ye tags nahi hone chahiye (LLMEngine already alag karta hai),
 * lekin agar kisi edge case mein aa bhi jayein toh dikhne do - strip mat karo.
 */
fun stripXmlLikeTags(text: String): String {
    if ('<' !in text) return text

    val sb = StringBuilder(text.length)
    var i = 0
    val len = text.length

    while (i < len) {
        val c = text[i]
        if (c != '<') {
            sb.append(c)
            i++
            continue
        }

        val gt = text.indexOf('>', i + 1)

        if (gt == -1) {
            // Incomplete tag at stream end - keep as-is
            sb.append(c)
            i++
            continue
        }

        val inner = text.substring(i + 1, gt).trim()
        val tagName = inner.trimStart('/').substringBefore(' ').substringBefore('\n').trim().lowercase()

        // Reasoning tags - PASS THROUGH, do not strip
        if (tagName in REASONING_TAG_NAMES) {
            sb.append(text, i, gt + 1)
            i = gt + 1
            continue
        }

        val isXmlTag = tagName.isEmpty() || tagName.all { ch -> ch.isLetterOrDigit() || ch == '_' || ch == '-' }

        if (!isXmlTag) {
            sb.append(c)
            i++
            continue
        }

        // Valid chat template tag - skip it
        val isClosing = inner.startsWith('/')
        if (!isClosing) {
            val closeTag = "</$tagName>"
            val closeIdx = text.indexOf(closeTag, gt + 1, ignoreCase = true)
            if (closeIdx != -1) {
                i = closeIdx + closeTag.length
                if (i < len && text[i] == '\n') i++
                continue
            }
        }

        i = gt + 1
        if (i < len && text[i] == '\n') i++
    }

    return sb.toString().trimStart('\n')
}

private sealed class MdSegment {
    data class Text(val content: String) : MdSegment()
    data class Code(val language: String, val code: String) : MdSegment()
}

private fun parseSegments(markdown: String, isStreaming: Boolean = false): List<MdSegment> {
    if (!markdown.contains("```")) return listOf(MdSegment.Text(markdown))

    val segments = mutableListOf<MdSegment>()
    var pos = 0
    val len = markdown.length

    while (pos < len) {
        val fenceStart = markdown.indexOf("```", pos)
        if (fenceStart == -1) {
            val text = markdown.substring(pos).trim()
            if (text.isNotEmpty()) segments.add(MdSegment.Text(text))
            break
        }

        if (fenceStart > pos) {
            val text = markdown.substring(pos, fenceStart).trim()
            if (text.isNotEmpty()) segments.add(MdSegment.Text(text))
        }

        val afterFence = fenceStart + 3
        val langEnd = markdown.indexOf('\n', afterFence)

        if (langEnd == -1) {
            if (isStreaming) {
                segments.add(MdSegment.Code(markdown.substring(afterFence).trim(), ""))
            }
            break
        }

        val lang = markdown.substring(afterFence, langEnd).trim()
        val codeStart = langEnd + 1
        val closingFence = markdown.indexOf("```", codeStart)

        if (closingFence == -1) {
            if (isStreaming) {
                val rawCode = markdown.substring(codeStart)
                    .trimEnd('\u258c')
                    .trimEnd('\n')
                segments.add(MdSegment.Code(lang, rawCode))
            } else {
                val text = markdown.substring(fenceStart).trim()
                if (text.isNotEmpty()) segments.add(MdSegment.Text(text))
            }
            break
        }

        segments.add(MdSegment.Code(lang, markdown.substring(codeStart, closingFence).trimEnd('\n')))
        pos = closingFence + 3
    }

    return if (segments.isEmpty()) listOf(MdSegment.Text(markdown)) else segments
}

/**
 * MarkdownText — pure Compose Text() only.
 * Code blocks = CodeBlock composable (apna highlight engine).
 * XML tags (</think> etc.) always stripped before render.
 */
@Composable
fun MarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = NeonText,
    fontSize: Float = 16f,
    isSelectable: Boolean = true,
    isStreaming: Boolean = false
) {
    // Strip XML tags first (O(n) single pass, negligible cost vs render)
    val stripped = stripXmlLikeTags(markdown)
    val renderText = if (isStreaming) stripped else closeIncompleteCodeBlocks(stripped)

    val segments = if (isStreaming) {
        parseSegments(renderText, isStreaming = true)
    } else {
        remember(renderText) { parseSegments(renderText, isStreaming = false) }
    }

    // lineHeight: fontSize * 1.42 ~ 22sp for 15.5sp — consistent across streaming & final
    val lineHeightSp = (fontSize * 1.42f).sp

    // Fast path: single plain text segment
    if (segments.size == 1 && segments[0] is MdSegment.Text) {
        val text = (segments[0] as MdSegment.Text).content
        if (isSelectable) {
            SelectionContainer(modifier = modifier) {
                Text(text = text, color = color, fontSize = fontSize.sp, lineHeight = lineHeightSp)
            }
        } else {
            Text(text = text, modifier = modifier, color = color, fontSize = fontSize.sp, lineHeight = lineHeightSp)
        }
        return
    }

    // Mixed: text + code blocks
    Column(modifier = modifier.fillMaxWidth()) {
        segments.forEachIndexed { index, segment ->
            when (segment) {
                is MdSegment.Text -> {
                    if (segment.content.isNotBlank()) {
                        if (isSelectable) {
                            SelectionContainer {
                                Text(
                                    text = segment.content,
                                    color = color,
                                    fontSize = fontSize.sp,
                                    lineHeight = lineHeightSp
                                )
                            }
                        } else {
                            Text(
                                text = segment.content,
                                color = color,
                                fontSize = fontSize.sp,
                                lineHeight = lineHeightSp
                            )
                        }
                    }
                }
                is MdSegment.Code -> {
                    if (index > 0) Spacer(modifier = Modifier.height(8.dp))
                    CodeBlock(
                        code = segment.code,
                        language = segment.language,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (index < segments.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}
