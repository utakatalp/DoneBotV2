package com.utakatalp.donebot.ui.onboarding

import androidx.compose.runtime.Immutable

object OnboardingContract {
    @Immutable
    data class UiState(
        val bgIndex: Int = 0,
    )

    sealed interface UiAction {
        data object OnLoginTap : UiAction
        data object OnGetStartedTap : UiAction
    }
}
