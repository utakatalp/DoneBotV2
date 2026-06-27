package com.utakatalp.donebot.ui.addtask

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalTime

object AddTaskContract {

    @Immutable
    data class UiState(
        val title: String = "",
        val description: String = "",
        val date: LocalDate? = null,
        val timeStart: LocalTime? = null,
        val timeEnd: LocalTime? = null,
        val isDatePickerOpen: Boolean = false,
        val isStartTimePickerOpen: Boolean = false,
        val isEndTimePickerOpen: Boolean = false,
        val titleError: AddTaskError? = null,
        val dateError: AddTaskError? = null,
        val timeError: AddTaskError? = null,
        val isSaving: Boolean = false,
    )

    sealed interface UiAction {
        data class OnTitleChange(val value: String) : UiAction
        data class OnDescriptionChange(val value: String) : UiAction
        data object OnDateTap : UiAction
        data object OnDateDismiss : UiAction
        data class OnDatePicked(val date: LocalDate) : UiAction
        data object OnStartTimeTap : UiAction
        data object OnStartTimeDismiss : UiAction
        data class OnStartTimePicked(val time: LocalTime) : UiAction
        data object OnEndTimeTap : UiAction
        data object OnEndTimeDismiss : UiAction
        data class OnEndTimePicked(val time: LocalTime) : UiAction
        data object OnSaveTap : UiAction
        data object OnDismissTap : UiAction
    }

    sealed interface UiEffect {
        data class ShowError(val message: String) : UiEffect
    }

    data class AddTaskError(val message: String)
}
