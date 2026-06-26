package com.utakatalp.donebot.ui.login

object LoginContract {
    data class State(
        val email: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed interface Event {
        data class EmailChanged(val value: String) : Event
        data class PasswordChanged(val value: String) : Event
        object LoginClicked : Event
        object RegisterClicked : Event
    }

    sealed interface Effect {
        object NavigateToHome : Effect
        object NavigateToRegister : Effect
        data class ShowError(val message: String) : Effect
    }
}
