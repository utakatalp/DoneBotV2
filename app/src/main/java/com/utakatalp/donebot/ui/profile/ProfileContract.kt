package com.utakatalp.donebot.ui.profile

import androidx.compose.runtime.Immutable

object ProfileContract {
    @Immutable
    data class UiState(
        val isAuthenticated: Boolean = false,
    )

    sealed interface UiAction {
        data object OnLoginTap : UiAction
        data object OnRegisterTap : UiAction
    }
}
