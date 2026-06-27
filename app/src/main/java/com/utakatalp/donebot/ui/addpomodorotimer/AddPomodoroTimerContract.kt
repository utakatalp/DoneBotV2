package com.utakatalp.donebot.ui.addpomodorotimer

import androidx.compose.runtime.Immutable

object AddPomodoroTimerContract {

    @Immutable
    data class UiState(
        val focusMinutes: Int = DEFAULT_FOCUS_MINUTES,
        val shortBreakMinutes: Int = DEFAULT_SHORT_BREAK_MINUTES,
        val longBreakMinutes: Int = DEFAULT_LONG_BREAK_MINUTES,
        val sessionCount: Int = DEFAULT_SESSION_COUNT,
        val sectionCount: Int = DEFAULT_SECTION_COUNT,
        val isSaving: Boolean = false,
    )

    sealed interface UiAction {
        data class OnFocusChange(val value: Int) : UiAction
        data class OnShortBreakChange(val value: Int) : UiAction
        data class OnLongBreakChange(val value: Int) : UiAction
        data class OnSessionCountChange(val value: Int) : UiAction
        data class OnSectionCountChange(val value: Int) : UiAction
        data object OnSaveTap : UiAction
        data object OnBackTap : UiAction
    }

    sealed interface UiEffect {
        data class ShowError(val message: String) : UiEffect
    }

    const val DEFAULT_FOCUS_MINUTES = 25
    const val DEFAULT_SHORT_BREAK_MINUTES = 5
    const val DEFAULT_LONG_BREAK_MINUTES = 15
    const val DEFAULT_SESSION_COUNT = 4
    const val DEFAULT_SECTION_COUNT = 1

    const val FOCUS_MIN = 5
    const val FOCUS_MAX = 60
    const val SHORT_BREAK_MIN = 1
    const val SHORT_BREAK_MAX = 15
    const val LONG_BREAK_MIN = 5
    const val LONG_BREAK_MAX = 30
    const val SESSION_COUNT_MIN = 1
    const val SESSION_COUNT_MAX = 10
    const val SECTION_COUNT_MIN = 1
    const val SECTION_COUNT_MAX = 10
}
