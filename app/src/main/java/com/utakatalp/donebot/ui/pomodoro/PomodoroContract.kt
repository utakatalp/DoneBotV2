package com.utakatalp.donebot.ui.pomodoro

import androidx.compose.runtime.Immutable
import com.utakatalp.donebot.domain.engine.PomodoroMode

object PomodoroContract {

    @Immutable
    data class UiState(
        val remainingSeconds: Long = 0L,
        val currentSessionTotalSeconds: Long = 0L,
        val mode: PomodoroMode = PomodoroMode.Focus,
        val isRunning: Boolean = false,
        val currentSessionIndex: Int = 0,
        val totalSessions: Int = 0,
        val showFinishEarlyDialog: Boolean = false,
    ) {
        val progress: Float
            get() = if (currentSessionTotalSeconds > 0L) {
                (currentSessionTotalSeconds - remainingSeconds).toFloat() / currentSessionTotalSeconds
            } else {
                0f
            }
        val minutes: Long get() = remainingSeconds / 60L
        val seconds: Long get() = remainingSeconds % 60L
    }

    sealed interface UiAction {
        data object OnPlayPauseTap : UiAction
        data object OnSkipTap : UiAction
        data object OnFinishTap : UiAction
        data object OnConfirmFinish : UiAction
        data object OnDismissFinishDialog : UiAction
        data object OnBackTap : UiAction
    }

    sealed interface UiEffect {
        data object SessionFinished : UiEffect
    }
}
