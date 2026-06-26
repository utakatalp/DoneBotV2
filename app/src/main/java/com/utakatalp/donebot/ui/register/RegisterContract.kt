package com.utakatalp.donebot.ui.register

object RegisterContract {
    data class State(
        val email: String = "",
        val password: String = "",
        val confirmPassword: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed interface Event {
        data class EmailChanged(val value: String) : Event
        data class PasswordChanged(val value: String) : Event
        data class ConfirmPasswordChanged(val value: String) : Event
        object RegisterClicked : Event
        object LoginClicked : Event
    }

    sealed interface Effect {
        object NavigateToHome : Effect
        object NavigateToLogin : Effect
        data class ShowError(val message: String) : Effect
    }
}
