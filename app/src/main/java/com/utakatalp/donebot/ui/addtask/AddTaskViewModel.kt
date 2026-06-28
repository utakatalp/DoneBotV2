package com.utakatalp.donebot.ui.addtask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.usecase.AddTaskUseCase
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.ui.addtask.AddTaskContract.AddTaskError
import com.utakatalp.donebot.ui.addtask.AddTaskContract.UiAction
import com.utakatalp.donebot.ui.addtask.AddTaskContract.UiEffect
import com.utakatalp.donebot.ui.addtask.AddTaskContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class AddTaskViewModel @Inject constructor(
    private val addTaskUseCase: AddTaskUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    private var hasSubmittedOnce = false

    fun onAction(action: UiAction) {
        when (action) {
            is UiAction.OnTitleChange -> updateTitle(action.value)
            is UiAction.OnDescriptionChange -> _uiState.update { it.copy(description = action.value) }
            is UiAction.OnDatePicked -> updateDate(action.date)
            is UiAction.OnStartTimePicked -> updateStartTime(action.time)
            is UiAction.OnEndTimePicked -> updateEndTime(action.time)
            UiAction.OnDateTap -> _uiState.update { it.copy(isDatePickerOpen = true) }
            UiAction.OnDateDismiss -> _uiState.update { it.copy(isDatePickerOpen = false) }
            UiAction.OnStartTimeTap -> _uiState.update { it.copy(isStartTimePickerOpen = true) }
            UiAction.OnStartTimeDismiss -> _uiState.update { it.copy(isStartTimePickerOpen = false) }
            UiAction.OnEndTimeTap -> _uiState.update { it.copy(isEndTimePickerOpen = true) }
            UiAction.OnEndTimeDismiss -> _uiState.update { it.copy(isEndTimePickerOpen = false) }
            UiAction.OnSaveTap -> trySave()
            UiAction.OnDismissTap -> emitNav(NavigationEffect.GoBack)
        }
    }

    private fun updateTitle(value: String) = _uiState.update {
        it.copy(
            title = value,
            titleError = if (hasSubmittedOnce) validateTitle(value) else null,
        )
    }

    private fun updateDate(date: LocalDate) = _uiState.update {
        it.copy(
            date = date,
            isDatePickerOpen = false,
            dateError = if (hasSubmittedOnce) validateDate(date) else null,
        )
    }

    private fun updateStartTime(time: LocalTime) = _uiState.update {
        it.copy(
            timeStart = time,
            isStartTimePickerOpen = false,
            timeError = if (hasSubmittedOnce) validateTimes(time, it.timeEnd) else null,
        )
    }

    private fun updateEndTime(time: LocalTime) = _uiState.update {
        it.copy(
            timeEnd = time,
            isEndTimePickerOpen = false,
            timeError = if (hasSubmittedOnce) validateTimes(it.timeStart, time) else null,
        )
    }

    private fun trySave() {
        hasSubmittedOnce = true
        val state = _uiState.value
        val titleError = validateTitle(state.title)
        val dateError = validateDate(state.date)
        val timeError = validateTimes(state.timeStart, state.timeEnd)

        if (titleError != null || dateError != null || timeError != null) {
            _uiState.update {
                it.copy(
                    titleError = titleError,
                    dateError = dateError,
                    timeError = timeError,
                )
            }
            return
        }
        addTask()
    }

    private fun addTask() = viewModelScope.launch {
        val state = _uiState.value
        _uiState.update { it.copy(isSaving = true) }
        addTaskUseCase(state.toNewTask())
            .onSuccess { emitNav(NavigationEffect.GoBack) }
            .onFailure { emitEffect(UiEffect.ShowError(it.message ?: "Failed to save task")) }
        _uiState.update { it.copy(isSaving = false) }
    }

    private fun UiState.toNewTask(): Task = Task(
        title = title.trim(),
        description = description.trim().takeIf { it.isNotBlank() },
        date = date!!,
        timeStart = timeStart!!,
        timeEnd = timeEnd!!,
    )

    private fun validateTitle(title: String): AddTaskError? = when {
        title.isBlank() -> AddTaskError("Please enter a title")
        else -> null
    }

    private fun validateDate(date: LocalDate?): AddTaskError? = when (date) {
        null -> AddTaskError("Pick a date")
        else -> null
    }

    private fun validateTimes(start: LocalTime?, end: LocalTime?): AddTaskError? = when {
        start == null || end == null -> AddTaskError("Pick start and end times")
        !end.isAfter(start) -> AddTaskError("End time must be after start time")
        else -> null
    }

    private fun emitEffect(effect: UiEffect) = viewModelScope.launch { _uiEffect.send(effect) }
    private fun emitNav(effect: NavigationEffect) = viewModelScope.launch { _navEffect.send(effect) }
}
