package com.utakatalp.donebot.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.common.needsOverlayPermission
import com.utakatalp.donebot.common.needsPostNotificationsPermission
import com.utakatalp.donebot.domain.engine.PomodoroEngine
import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.repository.TaskRepository
import com.utakatalp.donebot.domain.usecase.DeleteTaskUseCase
import com.utakatalp.donebot.domain.usecase.FetchTasksUseCase
import com.utakatalp.donebot.domain.usecase.UpdateTaskUseCase
import com.utakatalp.donebot.navigation.AddTask
import com.utakatalp.donebot.navigation.Details
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.navigation.Pomodoro
import com.utakatalp.donebot.navigation.PomodoroLaunch
import com.utakatalp.donebot.ui.home.HomeContract.UiAction
import com.utakatalp.donebot.ui.home.HomeContract.UiEffect
import com.utakatalp.donebot.ui.home.HomeContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

private const val REFRESH_INDICATOR_MIN_MS = 1500L

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val taskRepository: TaskRepository,
    private val fetchTasksUseCase: FetchTasksUseCase,
    private val pomodoroEngine: PomodoroEngine,
    private val updateTaskUseCase: UpdateTaskUseCase,
    private val deleteTaskUseCase: DeleteTaskUseCase,
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _displayedMonth = MutableStateFlow(YearMonth.now())
    private val _pendingDeleteTask = MutableStateFlow<Task?>(null)
    private val _isDeleteDialogOpen = MutableStateFlow(false)
    private val _isRefreshing = MutableStateFlow(false)
    private val _neededPermissions = MutableStateFlow(computeNeededPermissions())
    private val _dismissedPermissions = MutableStateFlow<Set<PermissionType>>(emptySet())

    // Bumped by OnRetry to force flatMapLatest to tear down the inner combine and re-subscribe
    // to all upstream source flows. Necessary because the .catch{} below terminates the inner
    // flow on error — there's no way to resume it from inside.
    private val _retryTrigger = MutableStateFlow(0)

    // Home has 8 source flows; combine() tops out at 5 args, so we pre-bundle the four
    // ephemeral UI flags into a typed Flags struct.
    private data class Flags(
        val dialogOpen: Boolean,
        val refreshing: Boolean,
        val visiblePrompts: List<PermissionType>,
    )

    private val flags: Flow<Flags> = combine(
        _isDeleteDialogOpen,
        _isRefreshing,
        _neededPermissions,
        _dismissedPermissions,
    ) { dialogOpen, refreshing, needed, dismissed ->
        Flags(dialogOpen, refreshing, (needed - dismissed).toList())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<UiState> = _retryTrigger.flatMapLatest {
        combine(
            _selectedDate,
            _displayedMonth,
            taskRepository.getTasks(),
            _pendingDeleteTask,
            flags,
        ) { date, month, allTasks, pendingDelete, f ->
            // Hoisted out of HomeContent — was running per-recomposition (every state change in
            // the screen), allocating a new List + new LocalDate and defeating stability skipping
            // on TDMonthlyDatePicker and HomeTaskList. Computed here means once per upstream emission.
            val visibleTasks = allTasks.filter { it.date == date && it.id != pendingDelete?.id }
            UiState.Success(
                selectedDate = date,
                displayedMonth = month,
                today = LocalDate.now(),
                tasks = visibleTasks,
                taskDates = allTasks.map { it.date }.toSet(),
                pendingDeleteTask = pendingDelete,
                isDeleteDialogOpen = f.dialogOpen,
                isRefreshing = f.refreshing,
                visiblePermissionPrompts = f.visiblePrompts,
            ) as UiState
        }.catch { error -> emit(UiState.Error(error.message ?: "Failed to load tasks")) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Loading)

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
            UiAction.OnPomodoroTap -> _navEffect.trySend(
                NavigationEffect.Navigate(
                    if (pomodoroEngine.state.value.isRunning) Pomodoro else PomodoroLaunch,
                ),
            )
            UiAction.OnDeleteDialogConfirm -> confirmDelete()
            UiAction.OnDeleteDialogDismiss -> dismissDeleteDialog()
            UiAction.OnRetry -> _retryTrigger.update { it + 1 }
            UiAction.OnRefresh -> refresh()
            UiAction.OnRecheckPermissions -> _neededPermissions.value = computeNeededPermissions()
            is UiAction.OnPermissionGranted -> {
                _dismissedPermissions.value = _dismissedPermissions.value - action.type
                _neededPermissions.value = computeNeededPermissions()
            }
            is UiAction.OnPermissionDismissed -> {
                _dismissedPermissions.value = _dismissedPermissions.value + action.type
            }
        }
    }

    private fun computeNeededPermissions(): Set<PermissionType> = buildSet {
        if (appContext.needsOverlayPermission()) add(PermissionType.OVERLAY)
        if (appContext.needsPostNotificationsPermission()) add(PermissionType.NOTIFICATION)
    }

    private fun refresh() {
        _isRefreshing.value = true
        fetchTasksUseCase(force = true)
        viewModelScope.launch {
            delay(REFRESH_INDICATOR_MIN_MS)
            _isRefreshing.value = false
        }
    }

    private fun toggleCompletion(task: Task) = viewModelScope.launch {
        updateTaskUseCase(task.copy(isCompleted = !task.isCompleted))
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
        deleteTaskUseCase(target.id)
            .onFailure { emitError(it) }
    }

    private fun emitError(error: Throwable) {
        _uiEffect.trySend(UiEffect.ShowError(error.message ?: "Something went wrong"))
    }
}
