package com.utakatalp.donebot.ui.pomodoro

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.domain.engine.PomodoroMode

@Composable
internal fun PomodoroSessionDots(
    currentIndex: Int,
    total: Int,
    mode: PomodoroMode,
    modifier: Modifier = Modifier,
) {
    if (total <= 0) return
    val activeColor = mode.toColor()
    val inactiveColor = TDTheme.colors.lightGray
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        repeat(total) { index ->
            val isCurrent = index == currentIndex
            val isPast = index < currentIndex
            val color = when {
                isCurrent -> activeColor
                isPast -> activeColor.copy(alpha = 0.5f)
                else -> inactiveColor
            }
            val dotSize = if (isCurrent) 12.dp else 8.dp
            Box(
                modifier = Modifier
                    .size(dotSize)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
