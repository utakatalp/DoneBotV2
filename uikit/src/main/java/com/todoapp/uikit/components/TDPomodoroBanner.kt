package com.todoapp.uikit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.previews.TDPreview
import com.todoapp.uikit.theme.TDTheme

@Composable
fun TDPomodoroBanner(
    modeLabel: String,
    modeIcon: ImageVector,
    modeColor: Color,
    timeText: String,
    isRunning: Boolean,
    onTap: () -> Unit,
    onPlayPauseTap: () -> Unit,
    onSkipTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp)
            .background(modeColor.copy(alpha = 0.10f))
            .clickable(onClick = onTap)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = modeIcon,
            contentDescription = null,
            tint = modeColor,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            TDText(
                text = modeLabel,
                style = TDTheme.typography.subheading4,
                color = TDTheme.colors.gray,
            )
            TDText(
                text = timeText,
                style = TDTheme.typography.heading4,
                color = TDTheme.colors.onBackground,
            )
        }
        IconButton(onClick = onPlayPauseTap) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = null,
                tint = modeColor,
            )
        }
        IconButton(onClick = onSkipTap) {
            Icon(
                imageVector = Icons.Default.SkipNext,
                contentDescription = null,
                tint = modeColor,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = TDTheme.colors.gray,
        )
    }
}

@TDPreview
@Composable
private fun TDPomodoroBannerFocusPreview() {
    TDTheme {
        TDPomodoroBanner(
            modeLabel = "Focus",
            modeIcon = androidx.compose.material.icons.Icons.Default.PlayArrow,
            modeColor = TDTheme.colors.pendingGray,
            timeText = "17:23",
            isRunning = true,
            onTap = {},
            onPlayPauseTap = {},
            onSkipTap = {},
        )
    }
}

@TDPreview
@Composable
private fun TDPomodoroBannerPausedPreview() {
    TDTheme {
        TDPomodoroBanner(
            modeLabel = "Short break",
            modeIcon = androidx.compose.material.icons.Icons.Default.PlayArrow,
            modeColor = TDTheme.colors.mediumGreen,
            timeText = "04:15",
            isRunning = false,
            onTap = {},
            onPlayPauseTap = {},
            onSkipTap = {},
        )
    }
}
