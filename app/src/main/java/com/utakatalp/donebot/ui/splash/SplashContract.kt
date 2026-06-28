package com.utakatalp.donebot.ui.splash

import com.utakatalp.donebot.navigation.AppKey
import com.utakatalp.donebot.navigation.Onboarding

object SplashContract {
    sealed interface UiState {
        data object Resolving : UiState
        data object EnterApp : UiState
        data class NeedsAuth(
            val startAt: AppKey = Onboarding,
            val cancelable: Boolean = false,
        ) : UiState
    }

    sealed interface UiAction {
        data object OnAuthenticated : UiAction
        data object OnCancelAuth : UiAction
        data object OnLoggedOut : UiAction
        data class OnRequestAuth(val startAt: AppKey) : UiAction
    }
}
