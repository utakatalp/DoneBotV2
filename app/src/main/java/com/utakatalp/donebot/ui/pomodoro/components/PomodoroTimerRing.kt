package com.utakatalp.donebot.ui.pomodoro.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.domain.engine.PomodoroMode

@Composable
internal fun PomodoroTimerRing(
    progress: Float,
    mode: PomodoroMode,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val trackColor = TDTheme.colors.lightPending
    val activeColor = mode.toColor()
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600),
        label = "pomodoro-progress",
    )

    Box(
        modifier = modifier.aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = 18.dp.toPx()
            val diameter = size.minDimension - strokePx
            val topLeft = Offset(
                x = (size.width - diameter) / 2f,
                y = (size.height - diameter) / 2f,
            )
            val arcSize = Size(diameter, diameter)
            val stroke = Stroke(width = strokePx, cap = StrokeCap.Round)
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            drawArc(
                color = activeColor,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
        }
        content()
    }
}

@Composable
internal fun PomodoroTimerRingDimmed(modifier: Modifier = Modifier) {
    // Placeholder for transitional states (queue empty). Renders an empty ring.
    Canvas(modifier = modifier.aspectRatio(1f)) {
        val strokePx = 18.dp.toPx()
        val diameter = size.minDimension - strokePx
        val topLeft = Offset(
            x = (size.width - diameter) / 2f,
            y = (size.height - diameter) / 2f,
        )
        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = topLeft,
            size = Size(diameter, diameter),
            style = Stroke(width = strokePx, cap = StrokeCap.Round),
        )
    }
}
