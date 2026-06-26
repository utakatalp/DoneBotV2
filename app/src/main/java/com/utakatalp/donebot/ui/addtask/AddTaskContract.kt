package com.utakatalp.donebot.ui.addtask

object AddTaskContract {
    data class State(
        val title: String = "",
        val description: String = "",
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed interface Event {
        data class TitleChanged(val value: String) : Event
        data class DescriptionChanged(val value: String) : Event
        object SaveClicked : Event
        object BackClicked : Event
    }

    sealed interface Effect {
        object NavigateBack : Effect
        data class ShowError(val message: String) : Effect
    }
}
