package com.utakatalp.donebot.ui.pomodoro.edit

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.components.TDButton
import com.todoapp.uikit.components.TDButtonType
import com.todoapp.uikit.components.TDText
import com.todoapp.uikit.previews.TDPreview
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.R
import com.utakatalp.donebot.ui.pomodoro.edit.AddPomodoroTimerContract.UiAction
import com.utakatalp.donebot.ui.pomodoro.edit.AddPomodoroTimerContract.UiEffect
import com.utakatalp.donebot.ui.pomodoro.edit.AddPomodoroTimerContract.UiState
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun AddPomodoroTimerScreen(
    uiState: UiState,
    uiEffect: Flow<UiEffect>,
    onAction: (UiAction) -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowError -> Toast.makeText(context, effect.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TDTheme.colors.background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onAction(UiAction.OnBackTap) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.pomodoro_back),
                        tint = TDTheme.colors.onBackground,
                    )
                }
                Spacer(Modifier.size(8.dp))
                TDText(
                    text = stringResource(R.string.pomodoro_edit_settings_title),
                    style = TDTheme.typography.heading2,
                    color = TDTheme.colors.onBackground,
                )
            }

            StepperRow(
                label = stringResource(R.string.pomodoro_focus_minutes),
                value = uiState.focusMinutes,
                unit = stringResource(R.string.pomodoro_unit_minutes),
                onValueChange = { onAction(UiAction.OnFocusChange(it)) },
                min = AddPomodoroTimerContract.FOCUS_MIN,
                max = AddPomodoroTimerContract.FOCUS_MAX,
                step = 5,
            )
            StepperRow(
                label = stringResource(R.string.pomodoro_short_break_minutes),
                value = uiState.shortBreakMinutes,
                unit = stringResource(R.string.pomodoro_unit_minutes),
                onValueChange = { onAction(UiAction.OnShortBreakChange(it)) },
                min = AddPomodoroTimerContract.SHORT_BREAK_MIN,
                max = AddPomodoroTimerContract.SHORT_BREAK_MAX,
                step = 1,
            )
            StepperRow(
                label = stringResource(R.string.pomodoro_long_break_minutes),
                value = uiState.longBreakMinutes,
                unit = stringResource(R.string.pomodoro_unit_minutes),
                onValueChange = { onAction(UiAction.OnLongBreakChange(it)) },
                min = AddPomodoroTimerContract.LONG_BREAK_MIN,
                max = AddPomodoroTimerContract.LONG_BREAK_MAX,
                step = 5,
            )
            StepperRow(
                label = stringResource(R.string.pomodoro_sessions),
                value = uiState.sessionCount,
                unit = stringResource(R.string.pomodoro_unit_sessions),
                onValueChange = { onAction(UiAction.OnSessionCountChange(it)) },
                min = AddPomodoroTimerContract.SESSION_COUNT_MIN,
                max = AddPomodoroTimerContract.SESSION_COUNT_MAX,
                step = 1,
            )
            StepperRow(
                label = stringResource(R.string.pomodoro_sections),
                value = uiState.sectionCount,
                unit = stringResource(R.string.pomodoro_unit_sections),
                onValueChange = { onAction(UiAction.OnSectionCountChange(it)) },
                min = AddPomodoroTimerContract.SECTION_COUNT_MIN,
                max = AddPomodoroTimerContract.SECTION_COUNT_MAX,
                step = 1,
            )

            Spacer(Modifier.height(8.dp))
            TDButton(
                text = stringResource(R.string.pomodoro_save),
                onClick = { onAction(UiAction.OnSaveTap) },
                type = TDButtonType.PRIMARY,
                fullWidth = true,
            )
        }
    }
}

@Composable
private fun StepperRow(
    label: String,
    value: Int,
    unit: String,
    onValueChange: (Int) -> Unit,
    min: Int,
    max: Int,
    step: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TDTheme.colors.lightPending, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            TDText(
                text = label,
                style = TDTheme.typography.heading6,
                color = TDTheme.colors.onSurface,
            )
            TDText(
                text = "$value $unit",
                style = TDTheme.typography.subheading4,
                color = TDTheme.colors.gray,
            )
        }
        StepperButton(
            symbol = "−",
            enabled = value > min,
            onClick = { onValueChange(value - step) },
        )
        Spacer(Modifier.size(12.dp))
        StepperButton(
            symbol = "+",
            enabled = value < max,
            onClick = { onValueChange(value + step) },
        )
    }
}

@Composable
private fun StepperButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    val containerColor = if (enabled) TDTheme.colors.pendingGray else TDTheme.colors.lightGray
    val contentColor = if (enabled) TDTheme.colors.white else TDTheme.colors.gray
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(containerColor),
        contentAlignment = Alignment.Center,
    ) {
        IconButton(onClick = onClick, enabled = enabled) {
            TDText(
                text = symbol,
                style = TDTheme.typography.heading3.copy(fontWeight = FontWeight.Bold),
                color = contentColor,
            )
        }
    }
}

// region Previews

@TDPreview
@Composable
private fun AddPomodoroTimerDefaultsPreview() {
    DoneBotTheme {
        AddPomodoroTimerScreen(uiState = UiState(), uiEffect = emptyFlow(), onAction = {})
    }
}

@TDPreview
@Composable
private fun AddPomodoroTimerCustomPreview() {
    DoneBotTheme {
        AddPomodoroTimerScreen(
            uiState = UiState(
                focusMinutes = 50,
                shortBreakMinutes = 10,
                longBreakMinutes = 25,
                sessionCount = 3,
                sectionCount = 2,
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

// endregion
