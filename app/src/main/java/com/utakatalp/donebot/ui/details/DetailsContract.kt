package com.utakatalp.donebot.ui.details

import com.utakatalp.donebot.domain.model.Task

object DetailsContract {
    data class State(
        val task: Task? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed interface Event {
        object BackClicked : Event
        object DeleteClicked : Event
        data class CompletedToggled(val isCompleted: Boolean) : Event
    }

    sealed interface Effect {
        object NavigateBack : Effect
        data class ShowError(val message: String) : Effect
    }
}
