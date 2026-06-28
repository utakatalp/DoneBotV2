package com.utakatalp.donebot.ui.pomodoro.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.R
import com.utakatalp.donebot.domain.engine.PomodoroMode

@Composable
@ReadOnlyComposable
internal fun PomodoroMode.toColor(): Color = when (this) {
    PomodoroMode.Focus -> TDTheme.colors.pendingGray
    PomodoroMode.ShortBreak -> TDTheme.colors.mediumGreen
    PomodoroMode.LongBreak -> TDTheme.colors.darkPending
}

internal fun PomodoroMode.toIcon(): ImageVector = when (this) {
    PomodoroMode.Focus -> Icons.Default.Psychology
    PomodoroMode.ShortBreak -> Icons.Default.Coffee
    PomodoroMode.LongBreak -> Icons.Default.Hotel
}

@Composable
@ReadOnlyComposable
internal fun PomodoroMode.toLabel(): String = stringResource(
    when (this) {
        PomodoroMode.Focus -> R.string.pomodoro_mode_focus
        PomodoroMode.ShortBreak -> R.string.pomodoro_mode_short_break
        PomodoroMode.LongBreak -> R.string.pomodoro_mode_long_break
    },
)
