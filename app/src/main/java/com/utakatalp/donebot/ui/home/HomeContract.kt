package com.utakatalp.donebot.ui.home

import androidx.compose.runtime.Immutable
import com.utakatalp.donebot.domain.model.Task
import java.time.LocalDate
import java.time.YearMonth

object HomeContract {

    sealed interface UiState {
        data object Loading : UiState

        @Immutable
        data class Success(
            val selectedDate: LocalDate,
            val displayedMonth: YearMonth = YearMonth.from(selectedDate),
            val tasks: List<Task> = emptyList(),
            val taskDates: Set<LocalDate> = emptySet(),
            val pendingDeleteTask: Task? = null,
            val isDeleteDialogOpen: Boolean = false,
            val isRefreshing: Boolean = false,
            val visiblePermissionPrompts: List<PermissionType> = emptyList(),
        ) : UiState

        data class Error(val message: String) : UiState
    }

    sealed interface UiAction {
        data class OnDateSelect(val date: LocalDate) : UiAction
        data object OnPreviousMonth : UiAction
        data object OnNextMonth : UiAction
        data class OnTaskCheck(val task: Task) : UiAction
        data class OnTaskClick(val task: Task) : UiAction
        data class OnTaskLongPress(val task: Task) : UiAction
        data object OnAddTaskTap : UiAction
        data object OnPomodoroTap : UiAction
        data object OnDeleteDialogConfirm : UiAction
        data object OnDeleteDialogDismiss : UiAction
        data object OnUndoDelete : UiAction
        data object OnRetry : UiAction
        data object OnRefresh : UiAction
        data object RecheckPermissions : UiAction
        data class PermissionGranted(val type: PermissionType) : UiAction
        data class DismissPermission(val type: PermissionType) : UiAction
    }

    sealed interface UiEffect {
        data class ShowToast(val message: String) : UiEffect
        data class ShowError(val message: String) : UiEffect
    }
}
