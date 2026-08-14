package com.tk854.localmind.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tk854.localmind.ui.theme.*

@Composable
fun ThinkingBubble(
    reasoning: String,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    thinkingDurationSec: Int? = null
) {
    // DEFAULT COLLAPSED â€” user must manually expand
    // Button stays fixed, reasoning streams silently in background when collapsed
    var userExpanded by remember { mutableStateOf(false) }

    // Auto-collapse when streaming ends (answer is coming)
    // Do NOT auto-expand â€” user decides when to open
    LaunchedEffect(isStreaming) {
        if (!isStreaming) {
            userExpanded = false
        }
    }

    val arrowRotation by animateFloatAsState(
        targetValue = if (userExpanded) 180f else 0f,
        animationSpec = tween(250),
        label = "arrowRotation"
    )

    // NO animateContentSize() on outer Column â€” causes header/button to shift position
    // Header is always at fixed position, only content below expands/collapses
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        // FIXED HEADER â€” never moves regardless of content below
        ThinkingHeader(
            isStreaming = isStreaming,
            thinkingDurationSec = thinkingDurationSec,
            isExpanded = userExpanded,
            arrowRotation = arrowRotation,
            hasReasoning = reasoning.trim().isNotEmpty(),
            onToggle = { userExpanded = !userExpanded }
        )

        // Content expands/collapses below the fixed header
        AnimatedVisibility(
            visible = userExpanded,
            enter = expandVertically(tween(220)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(200)) + fadeOut(tween(160))
        ) {
            ReasoningText(
                reasoning = reasoning,
                isStreaming = isStreaming
            )
        }
    }
}

@Composable
private fun ThinkingHeader(
    isStreaming: Boolean,
    thinkingDurationSec: Int?,
    isExpanded: Boolean,
    arrowRotation: Float,
    hasReasoning: Boolean,
    onToggle: () -> Unit
) {
    // Fixed height row â€” always same size, never shifts
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(32.dp), // Fixed height â€” button never moves
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clickable(onClick = onToggle)
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (isStreaming) {
                ThinkingDotsText()
            } else {
                val label = when {
                    thinkingDurationSec != null && thinkingDurationSec > 0 ->
                        "Thought for ${thinkingDurationSec}s"
                    else -> "Thought"
                }
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = NeonTextSecondary
                )
            }
            // Arrow always visible when there is reasoning or streaming
            if (hasReasoning || isStreaming) {
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = NeonPrimary.copy(alpha = 0.7f),
                    modifier = Modifier
                        .size(16.dp)
                        .rotate(arrowRotation)
                )
            }
        }
    }
}

@Composable
private fun ThinkingDotsText() {
    val shimmerColors = listOf(
        NeonTextSecondary.copy(alpha = 0.3f),
        NeonTextSecondary.copy(alpha = 1.0f),
        NeonTextSecondary.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition(label = "thinking")

    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dotsPhase"
    )

    val dots = when {
        phase < 0.33f -> "..."
        phase < 0.66f -> ".."
        else -> "."
    }

    val brush = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = shimmerColors,
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(x = translateAnim, y = translateAnim)
    )

    Text(
        text = "Thinking$dots",
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            brush = brush
        )
    )
}

@Composable
private fun ReasoningText(
    reasoning: String,
    isStreaming: Boolean
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(reasoning) {
        if (isStreaming && scrollState.maxValue > 0) {
            scrollState.scrollTo(scrollState.maxValue)
        }
    }

    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val maxHeight = (screenHeight * 0.40f).coerceIn(200.dp, 400.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, bottom = 8.dp)
    ) {
        // Vertical accent line
        Box(
            modifier = Modifier
                .padding(top = 2.dp, end = 10.dp)
                .width(2.dp)
                .heightIn(min = 20.dp, max = maxHeight)
                .background(
                    color = if (isStreaming) NeonPrimary.copy(alpha = 0.4f)
                            else NeonTextSecondary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(1.dp)
                )
        )

        Text(
            text = reasoning.trim().ifBlank { if (isStreaming) "..." else "" },
            modifier = Modifier
                .weight(1f)
                .heightIn(max = maxHeight)
                .verticalScroll(scrollState)
                .padding(end = 4.dp, bottom = 4.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                // Same size as chat text (15.5sp)
                fontSize = 15.5.sp,
                lineHeight = 23.sp,
                fontStyle = FontStyle.Normal
            ),
            color = NeonTextSecondary
        )
    }
}
