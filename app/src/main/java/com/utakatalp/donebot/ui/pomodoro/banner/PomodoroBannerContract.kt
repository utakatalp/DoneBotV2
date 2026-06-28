package com.utakatalp.donebot.ui.pomodoro.banner

import androidx.compose.runtime.Immutable
import com.utakatalp.donebot.domain.engine.PomodoroMode

object PomodoroBannerContract {

    @Immutable
    data class UiState(
        val mode: PomodoroMode = PomodoroMode.Focus,
        val remainingSeconds: Long = 0L,
        val isRunning: Boolean = false,
        val hasActiveSession: Boolean = false,
    ) {
        val minutes: Long get() = remainingSeconds / 60L
        val seconds: Long get() = remainingSeconds % 60L
    }

    sealed interface UiAction {
        data object OnTap : UiAction
        data object OnPlayPauseTap : UiAction
        data object OnSkipTap : UiAction
    }
}
