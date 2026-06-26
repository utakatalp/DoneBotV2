package com.todoapp.uikit.theme

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TimePickerColors
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun textFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TDTheme.colors.onSurface,
    unfocusedTextColor = TDTheme.colors.onSurface,
    focusedTrailingIconColor = TDTheme.colors.pendingGray,
    unfocusedTrailingIconColor = TDTheme.colors.gray,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    errorContainerColor = Color.Transparent,
    focusedLabelColor = TDTheme.colors.pendingGray,
    unfocusedLabelColor = TDTheme.colors.gray,
    errorLabelColor = TDTheme.colors.red,
    focusedBorderColor = TDTheme.colors.pendingGray,
    unfocusedBorderColor = TDTheme.colors.lightGray,
    errorBorderColor = TDTheme.colors.red,
    disabledContainerColor = TDTheme.colors.background.copy(alpha = 0.8f),
    disabledTextColor = TDTheme.colors.onSurface.copy(alpha = 0.38f),
    disabledBorderColor = TDTheme.colors.lightGray.copy(alpha = 0.38f),
    disabledLabelColor = TDTheme.colors.gray.copy(alpha = 0.38f),
    disabledTrailingIconColor = TDTheme.colors.gray.copy(alpha = 0.38f),
    cursorColor = TDTheme.colors.pendingGray,
    errorCursorColor = TDTheme.colors.red,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun timePickerColors(): TimePickerColors = TimePickerDefaults.colors(
    containerColor = TDTheme.colors.purple,
    timeSelectorSelectedContainerColor = TDTheme.colors.lightPurple,
    timeSelectorUnselectedContainerColor = TDTheme.colors.lightPurple,
)
