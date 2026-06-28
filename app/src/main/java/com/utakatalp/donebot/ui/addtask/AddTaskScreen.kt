package com.utakatalp.donebot.ui.addtask

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.todoapp.uikit.components.TDButton
import com.todoapp.uikit.components.TDButtonSize
import com.todoapp.uikit.components.TDButtonType
import com.todoapp.uikit.components.TDText
import com.todoapp.uikit.previews.TDPreview
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.R
import com.utakatalp.donebot.ui.addtask.AddTaskContract.AddTaskError
import com.utakatalp.donebot.ui.addtask.AddTaskContract.UiAction
import com.utakatalp.donebot.ui.addtask.AddTaskContract.UiEffect
import com.utakatalp.donebot.ui.addtask.AddTaskContract.UiState
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    uiState: UiState,
    uiEffect: Flow<UiEffect>,
    onAction: (UiAction) -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowError -> Toast.makeText(context, effect.message, Toast.LENGTH_LONG)
                    .show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TDText(
                text = stringResource(R.string.addtask_title),
                style = TDTheme.typography.heading2,
                color = TDTheme.colors.onSurface,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onAction(UiAction.OnDismissTap) }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.addtask_dismiss),
                    tint = TDTheme.colors.onSurface,
                )
            }
        }

        AddTaskForm(uiState = uiState, onAction = onAction)
    }

    if (uiState.isDatePickerOpen) {
        AddTaskDatePicker(
            initialDate = uiState.date ?: LocalDate.now(),
            onDismiss = { onAction(UiAction.OnDateDismiss) },
            onConfirm = { onAction(UiAction.OnDatePicked(it)) },
        )
    }
    if (uiState.isStartTimePickerOpen) {
        AddTaskTimePicker(
            initialTime = uiState.timeStart ?: LocalTime.of(9, 0),
            onDismiss = { onAction(UiAction.OnStartTimeDismiss) },
            onConfirm = { onAction(UiAction.OnStartTimePicked(it)) },
        )
    }
    if (uiState.isEndTimePickerOpen) {
        AddTaskTimePicker(
            initialTime = uiState.timeEnd ?: (uiState.timeStart?.plusHours(1) ?: LocalTime.of(
                10,
                0
            )),
            onDismiss = { onAction(UiAction.OnEndTimeDismiss) },
            onConfirm = { onAction(UiAction.OnEndTimePicked(it)) },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskDatePicker(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
) {
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            .toEpochMilli(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        colors = DatePickerDefaults.colors(
            containerColor = TDTheme.colors.bgColorPurple,
            selectedDayContainerColor = TDTheme.colors.pendingGray,
            selectedDayContentColor = TDTheme.colors.white,
            todayContentColor = TDTheme.colors.pendingGray,
            todayDateBorderColor = TDTheme.colors.pendingGray,
        ),
        confirmButton = {
            TextButton(onClick = {
                state.selectedDateMillis?.let { millis ->
                    val date =
                        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    onConfirm(date)
                } ?: onDismiss()
            }) {
                TDText(
                    text = stringResource(R.string.addtask_ok),
                    color = TDTheme.colors.pendingGray,
                    style = TDTheme.typography.heading4,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                TDText(
                    text = stringResource(R.string.addtask_cancel),
                    color = TDTheme.colors.gray,
                    style = TDTheme.typography.heading4,
                )
            }
        },
    ) {
        DatePicker(
            state = state
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddTaskTimePicker(
    initialTime: LocalTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalTime) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimePicker(
                    state = state
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TDButton(
                        text = stringResource(R.string.addtask_cancel),
                        onClick = onDismiss,
                        type = TDButtonType.OUTLINE,
                        size = TDButtonSize.SMALL,
                    )
                    androidx.compose.foundation.layout.Spacer(Modifier.padding(4.dp))
                    TDButton(
                        text = stringResource(R.string.addtask_ok),
                        onClick = { onConfirm(LocalTime.of(state.hour, state.minute)) },
                        type = TDButtonType.PRIMARY,
                        size = TDButtonSize.SMALL,
                    )
                }
            }
        }
    }
}

// region Previews

@TDPreview
@Composable
private fun AddTaskEmptyPreview() {
    DoneBotTheme {
        AddTaskScreen(uiState = UiState(), uiEffect = emptyFlow(), onAction = {})
    }
}

@TDPreview
@Composable
private fun AddTaskFilledPreview() {
    DoneBotTheme {
        AddTaskScreen(
            uiState = UiState(
                title = "Team standup",
                description = "Sprint blockers and progress",
                date = LocalDate.now(),
                timeStart = LocalTime.of(10, 0),
                timeEnd = LocalTime.of(10, 30),
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun AddTaskTitleErrorPreview() {
    DoneBotTheme {
        AddTaskScreen(
            uiState = UiState(
                titleError = AddTaskError("Please enter a title"),
                dateError = AddTaskError("Pick a date"),
                timeError = AddTaskError("Pick start and end times"),
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun AddTaskTimeOrderErrorPreview() {
    DoneBotTheme {
        AddTaskScreen(
            uiState = UiState(
                title = "Run",
                date = LocalDate.now(),
                timeStart = LocalTime.of(11, 0),
                timeEnd = LocalTime.of(10, 0),
                timeError = AddTaskError("End time must be after start time"),
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun AddTaskSavingPreview() {
    DoneBotTheme {
        AddTaskScreen(
            uiState = UiState(
                title = "Team standup",
                date = LocalDate.now(),
                timeStart = LocalTime.of(10, 0),
                timeEnd = LocalTime.of(10, 30),
                isSaving = true,
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

// endregion
