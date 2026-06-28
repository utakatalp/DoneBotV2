package com.utakatalp.donebot.ui.pomodoro.launch

import androidx.compose.runtime.Immutable

object PomodoroLaunchContract {

    @Immutable
    data class UiState(
        val focusMinutes: Int = 25,
        val shortBreakMinutes: Int = 5,
        val longBreakMinutes: Int = 15,
        val sessionCount: Int = 4,
        val sectionCount: Int = 1,
        val isLoading: Boolean = true,
    ) {
        val totalSeconds: Long
            get() = sectionCount.toLong() * (
                focusMinutes.toLong() * sessionCount * 60L +
                    shortBreakMinutes.toLong() * (sessionCount - 1).coerceAtLeast(0) * 60L +
                    longBreakMinutes.toLong() * 60L
                )
    }

    sealed interface UiAction {
        data object OnStartTap : UiAction
        data object OnEditTap : UiAction
        data object OnBackTap : UiAction
    }

    sealed interface UiEffect {
        data class ShowError(val message: String) : UiEffect
    }
}
