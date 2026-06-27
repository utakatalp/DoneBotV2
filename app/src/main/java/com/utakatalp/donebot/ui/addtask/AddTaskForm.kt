package com.utakatalp.donebot.ui.addtask

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.components.TDButton
import com.todoapp.uikit.components.TDButtonType
import com.todoapp.uikit.components.TDLabeledTextField
import com.todoapp.uikit.components.TDText
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.R
import com.utakatalp.donebot.ui.addtask.AddTaskContract.UiAction
import com.utakatalp.donebot.ui.addtask.AddTaskContract.UiState
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val DATE_FORMAT: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)
private val TIME_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

@Composable
internal fun AddTaskForm(
    uiState: UiState,
    onAction: (UiAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            TDLabeledTextField(
                title = stringResource(R.string.addtask_title_label),
                value = uiState.title,
                onValueChange = { onAction(UiAction.OnTitleChange(it)) },
                placeholder = stringResource(R.string.addtask_title_placeholder),
                singleLine = true,
                isError = uiState.titleError != null,
            )
            uiState.titleError?.let { ErrorText(it.message) }
        }

        PickerRow(
            label = stringResource(R.string.addtask_date_label),
            value = uiState.date?.format(DATE_FORMAT),
            placeholder = stringResource(R.string.addtask_date_placeholder),
            error = uiState.dateError?.message,
            onClick = { onAction(UiAction.OnDateTap) },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            PickerRow(
                label = stringResource(R.string.addtask_time_start_label),
                value = uiState.timeStart?.formatHm(),
                placeholder = stringResource(R.string.addtask_time_placeholder),
                error = null,
                onClick = { onAction(UiAction.OnStartTimeTap) },
                modifier = Modifier.weight(1f),
            )
            PickerRow(
                label = stringResource(R.string.addtask_time_end_label),
                value = uiState.timeEnd?.formatHm(),
                placeholder = stringResource(R.string.addtask_time_placeholder),
                error = null,
                onClick = { onAction(UiAction.OnEndTimeTap) },
                modifier = Modifier.weight(1f),
            )
        }
        uiState.timeError?.let { ErrorText(it.message) }

        TDLabeledTextField(
            title = stringResource(R.string.addtask_description_label),
            value = uiState.description,
            onValueChange = { onAction(UiAction.OnDescriptionChange(it)) },
            placeholder = stringResource(R.string.addtask_description_placeholder),
            minLines = 3,
        )

        TDButton(
            text = stringResource(R.string.addtask_save_button),
            onClick = { onAction(UiAction.OnSaveTap) },
            type = TDButtonType.PRIMARY,
            fullWidth = true,
            isEnable = !uiState.isSaving,
        )
    }
}

@Composable
private fun PickerRow(
    label: String,
    value: String?,
    placeholder: String,
    error: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        TDText(
            text = label,
            style = TDTheme.typography.subheading4,
            color = TDTheme.colors.gray,
        )
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = TDTheme.colors.lightPending,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TDText(
                text = value ?: placeholder,
                style = TDTheme.typography.heading6,
                color = if (value != null) TDTheme.colors.onSurface else TDTheme.colors.gray,
            )
        }
        if (error != null) {
            Spacer(Modifier.height(4.dp))
            ErrorText(error)
        }
    }
}

@Composable
private fun ErrorText(message: String) {
    TDText(
        text = message,
        style = TDTheme.typography.subheading1,
        color = TDTheme.colors.crossRed,
        modifier = Modifier.padding(top = 4.dp),
    )
}

private fun LocalTime.formatHm(): String = format(TIME_FORMAT)
