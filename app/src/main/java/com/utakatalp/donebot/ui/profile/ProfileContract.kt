package com.utakatalp.donebot.ui.profile

object ProfileContract {
    data class State(
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed interface Event {
        object LogoutClicked : Event
        object SettingsClicked : Event
    }

    sealed interface Effect {
        object NavigateToLogin : Effect
        object NavigateToSettings : Effect
        data class ShowError(val message: String) : Effect
    }
}
