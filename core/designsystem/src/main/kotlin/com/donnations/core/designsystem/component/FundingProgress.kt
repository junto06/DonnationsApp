package com.donnations.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun FundingProgress(
    raised: String,
    goal: String,
    percent: String,
    progress: Float,
    modifier: Modifier = Modifier,
    amountStyle: TextStyle = MaterialTheme.typography.bodySmall,
    barHeight: Dp = 6.dp,
    amountsAboveBar: Boolean = false,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (amountsAboveBar) {
            Amounts(raised, goal, percent, amountStyle)
            Bar(progress, barHeight)
        } else {
            Bar(progress, barHeight)
            Amounts(raised, goal, percent, amountStyle)
        }
    }
}

@Composable
private fun Amounts(raised: String, goal: String, percent: String, style: TextStyle) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)) {
                    append(raised)
                }
                withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurfaceVariant)) {
                    append(" $goal")
                }
            },
            style = style,
        )
        Text(
            text = percent,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Bar(progress: Float, height: Dp) {
    // Overfunded (>1) or NaN progress would otherwise draw past the track.
    val safe = if (progress.isNaN()) 0f else progress.coerceIn(0f, 1f)
    LinearProgressIndicator(
        progress = { safe },
        modifier = Modifier.fillMaxWidth().height(height),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {},
    )
}
