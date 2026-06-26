package com.utakatalp.donebot.ui.home

import com.utakatalp.donebot.domain.model.Task

object HomeContract {
    data class State(
        val tasks: List<Task> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
    )

    sealed interface Event {
        object AddTaskClicked : Event
        data class TaskClicked(val taskId: String) : Event
        data class TaskCompleted(val taskId: String, val isCompleted: Boolean) : Event
        data class TaskDeleted(val taskId: String) : Event
    }

    sealed interface Effect {
        object NavigateToAddTask : Effect
        data class NavigateToDetails(val taskId: String) : Effect
        data class ShowError(val message: String) : Effect
    }
}
