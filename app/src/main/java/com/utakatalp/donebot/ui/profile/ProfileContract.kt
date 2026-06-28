package com.utakatalp.donebot.ui.profile

import androidx.compose.runtime.Immutable

object ProfileContract {
    @Immutable
    data class UiState(
        val isAuthenticated: Boolean = false,
        val showLogoutDialog: Boolean = false,
    )

    sealed interface UiAction {
        data object OnLoginTap : UiAction
        data object OnRegisterTap : UiAction
        data object OnLogoutTap : UiAction
        data object OnLogoutConfirm : UiAction
        data object OnLogoutDismiss : UiAction
    }

    sealed interface UiEffect {
        data object Logout : UiEffect
    }
}
