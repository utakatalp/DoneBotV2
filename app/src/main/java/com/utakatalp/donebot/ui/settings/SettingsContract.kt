package com.utakatalp.donebot.ui.settings

object SettingsContract {
    data class State(
        val isLoading: Boolean = false
    )

    sealed interface Event {
        object BackClicked : Event
    }

    sealed interface Effect {
        object NavigateBack : Effect
    }
}
