package com.utakatalp.donebot.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.repository.TaskRepository
import com.utakatalp.donebot.navigation.AddTask
import com.utakatalp.donebot.navigation.Details
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.ui.home.HomeContract.UiAction
import com.utakatalp.donebot.ui.home.HomeContract.UiEffect
import com.utakatalp.donebot.ui.home.HomeContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    private val _pendingDeleteTask = MutableStateFlow<Task?>(null)
    private val _isDeleteDialogOpen = MutableStateFlow(false)

    val uiState: StateFlow<UiState> = combine(
        _selectedDate,
        _displayedMonth,
        taskRepository.getTasks(),
        _pendingDeleteTask,
        _isDeleteDialogOpen,
    ) { date, month, allTasks, pendingDelete, dialogOpen ->
        UiState.Success(
            selectedDate = date,
            displayedMonth = month,
            tasks = allTasks.filter { it.date == date },
            taskDates = allTasks.map { it.date }.toSet(),
            pendingDeleteTask = pendingDelete,
            isDeleteDialogOpen = dialogOpen,
        ) as UiState
    }
        .catch { error -> emit(UiState.Error(error.message ?: "Failed to load tasks")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    fun onAction(action: UiAction) {
        when (action) {
            is UiAction.OnDateSelect -> {
                _selectedDate.value = action.date
                _displayedMonth.value = YearMonth.from(action.date)
            }
            is UiAction.OnTaskCheck -> toggleCompletion(action.task)
            is UiAction.OnTaskClick -> _navEffect.trySend(NavigationEffect.Navigate(Details(action.task.id)))
            is UiAction.OnTaskLongPress -> openDeleteDialog(action.task)
            UiAction.OnPreviousMonth -> _displayedMonth.value = _displayedMonth.value.minusMonths(1)
            UiAction.OnNextMonth -> _displayedMonth.value = _displayedMonth.value.plusMonths(1)
            UiAction.OnAddTaskTap -> _navEffect.trySend(NavigationEffect.Navigate(AddTask))
            UiAction.OnDeleteDialogConfirm -> confirmDelete()
            UiAction.OnDeleteDialogDismiss -> dismissDeleteDialog()
            UiAction.OnUndoDelete -> Unit
            UiAction.OnRetry -> Unit
        }
    }

    private fun toggleCompletion(task: Task) = viewModelScope.launch {
        taskRepository.updateTask(task.copy(isCompleted = !task.isCompleted))
            .onFailure { emitError(it) }
    }

    private fun openDeleteDialog(task: Task) {
        _pendingDeleteTask.value = task
        _isDeleteDialogOpen.value = true
    }

    private fun dismissDeleteDialog() {
        _pendingDeleteTask.value = null
        _isDeleteDialogOpen.value = false
    }

    private fun confirmDelete() = viewModelScope.launch {
        val target = _pendingDeleteTask.value ?: return@launch
        _pendingDeleteTask.value = null
        _isDeleteDialogOpen.value = false
        taskRepository.deleteTask(target.id)
            .onFailure { emitError(it) }
    }

    private fun emitError(error: Throwable) {
        _uiEffect.trySend(UiEffect.ShowError(error.message ?: "Something went wrong"))
    }
}
