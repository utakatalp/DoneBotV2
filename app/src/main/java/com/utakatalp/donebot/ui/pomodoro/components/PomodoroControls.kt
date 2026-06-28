package com.utakatalp.donebot.ui.pomodoro.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.R

@Composable
internal fun PomodoroControls(
    isRunning: Boolean,
    onPlayPauseTap: () -> Unit,
    onSkipTap: () -> Unit,
    onFinishTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlIconButton(
            icon = Icons.Default.Stop,
            contentDescription = stringResource(R.string.pomodoro_finish),
            containerColor = TDTheme.colors.crossRed.copy(alpha = 0.15f),
            iconTint = TDTheme.colors.crossRed,
            onClick = onFinishTap,
        )
        PlayPauseButton(isRunning = isRunning, onClick = onPlayPauseTap)
        ControlIconButton(
            icon = Icons.Default.SkipNext,
            contentDescription = stringResource(R.string.pomodoro_skip),
            containerColor = TDTheme.colors.lightPending,
            iconTint = TDTheme.colors.pendingGray,
            onClick = onSkipTap,
        )
    }
}

@Composable
private fun PlayPauseButton(isRunning: Boolean, onClick: () -> Unit) {
    val containerColor = TDTheme.colors.pendingGray
    val contentColor = TDTheme.colors.white
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick, modifier = Modifier.size(80.dp)) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = stringResource(
                    if (isRunning) R.string.pomodoro_pause else R.string.pomodoro_resume,
                ),
                tint = contentColor,
                modifier = Modifier.size(36.dp),
            )
        }
    }
}

@Composable
private fun ControlIconButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
            )
        }
    }
}
