package com.utakatalp.donebot.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.components.TDMonthlyDatePicker
import com.todoapp.uikit.previews.TDPreview
import com.utakatalp.donebot.R
import com.utakatalp.donebot.ui.home.HomeContract.UiAction
import com.utakatalp.donebot.ui.home.HomeContract.UiState
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import java.time.LocalDate

@Composable
internal fun HomeContent(
    uiState: UiState.Success,
    onAction: (UiAction) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            TDMonthlyDatePicker(
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                displayedMonth = uiState.displayedMonth,
                selectedDate = uiState.selectedDate,
                taskDates = uiState.taskDates,
                onDateSelect = { onAction(UiAction.OnDateSelect(it)) },
                onPreviousMonth = { onAction(UiAction.OnPreviousMonth) },
                onNextMonth = { onAction(UiAction.OnNextMonth) },
                today = LocalDate.now(),
            )
            HomeTaskList(
                tasks = uiState.tasks.filter { it.id != uiState.pendingDeleteTask?.id },
                onTaskCheck = { onAction(UiAction.OnTaskCheck(it)) },
                onTaskClick = { onAction(UiAction.OnTaskClick(it)) },
                onTaskLongPress = { onAction(UiAction.OnTaskLongPress(it)) },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            )
        }

        FloatingActionButton(
            onClick = { onAction(UiAction.OnAddTaskTap) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.home_add_task),
            )
        }

        if (uiState.isDeleteDialogOpen) {
            AlertDialog(
                onDismissRequest = { onAction(UiAction.OnDeleteDialogDismiss) },
                title = { Text(stringResource(R.string.home_delete_task_title)) },
                text = { Text(stringResource(R.string.home_delete_task_message)) },
                confirmButton = {
                    TextButton(onClick = { onAction(UiAction.OnDeleteDialogConfirm) }) {
                        Text(
                            text = stringResource(R.string.home_delete),
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(UiAction.OnDeleteDialogDismiss) }) {
                        Text(stringResource(R.string.home_cancel))
                    }
                },
            )
        }
    }
}

// region Previews

@TDPreview
@Composable
private fun HomeContentWithTasksPreview() {
    DoneBotTheme {
        HomeContent(
            uiState = UiState.Success(
                selectedDate = LocalDate.now(),
                tasks = previewSampleTasks(),
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun HomeContentEmptyPreview() {
    DoneBotTheme {
        HomeContent(
            uiState = UiState.Success(
                selectedDate = LocalDate.now(),
                tasks = emptyList(),
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun HomeContentDeleteDialogPreview() {
    val sample = previewSampleTasks()
    DoneBotTheme {
        HomeContent(
            uiState = UiState.Success(
                selectedDate = LocalDate.now(),
                tasks = sample,
                pendingDeleteTask = sample.first(),
                isDeleteDialogOpen = true,
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun HomeContentOffTodayPreview() {
    DoneBotTheme {
        HomeContent(
            uiState = UiState.Success(
                selectedDate = LocalDate.now().plusDays(3),
                tasks = emptyList(),
            ),
            onAction = {},
        )
    }
}

// endregion
