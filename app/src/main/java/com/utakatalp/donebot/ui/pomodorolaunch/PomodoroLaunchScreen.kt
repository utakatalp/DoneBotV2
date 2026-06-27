package com.utakatalp.donebot.ui.pomodorolaunch

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.components.TDButton
import com.todoapp.uikit.components.TDButtonType
import com.todoapp.uikit.components.TDText
import com.todoapp.uikit.previews.TDPreview
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.R
import com.utakatalp.donebot.ui.pomodorolaunch.PomodoroLaunchContract.UiAction
import com.utakatalp.donebot.ui.pomodorolaunch.PomodoroLaunchContract.UiEffect
import com.utakatalp.donebot.ui.pomodorolaunch.PomodoroLaunchContract.UiState
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun PomodoroLaunchScreen(
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
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
                    text = stringResource(R.string.pomodoro_launch_title),
                    style = TDTheme.typography.heading2,
                    color = TDTheme.colors.onBackground,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { onAction(UiAction.OnEditTap) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.pomodoro_edit_settings_title),
                        tint = TDTheme.colors.onBackground,
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryTile(
                    label = stringResource(R.string.pomodoro_focus_minutes_short),
                    value = uiState.focusMinutes.toString(),
                    unit = stringResource(R.string.pomodoro_unit_min),
                    modifier = Modifier.weight(1f),
                )
                SummaryTile(
                    label = stringResource(R.string.pomodoro_short_break_short),
                    value = uiState.shortBreakMinutes.toString(),
                    unit = stringResource(R.string.pomodoro_unit_min),
                    modifier = Modifier.weight(1f),
                )
                SummaryTile(
                    label = stringResource(R.string.pomodoro_long_break_short),
                    value = uiState.longBreakMinutes.toString(),
                    unit = stringResource(R.string.pomodoro_unit_min),
                    modifier = Modifier.weight(1f),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                SummaryTile(
                    label = stringResource(R.string.pomodoro_sessions),
                    value = uiState.sessionCount.toString(),
                    unit = "",
                    modifier = Modifier.weight(1f),
                )
                SummaryTile(
                    label = stringResource(R.string.pomodoro_sections),
                    value = uiState.sectionCount.toString(),
                    unit = "",
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.height(8.dp))

            TotalTimeRow(totalSeconds = uiState.totalSeconds)

            Spacer(Modifier.weight(1f))

            TDButton(
                text = stringResource(R.string.pomodoro_start),
                onClick = { onAction(UiAction.OnStartTap) },
                type = TDButtonType.PRIMARY,
                fullWidth = true,
                isEnable = !uiState.isLoading,
            )
        }
    }
}

@Composable
private fun SummaryTile(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(TDTheme.colors.lightPending, RoundedCornerShape(16.dp))
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TDText(
            text = label,
            style = TDTheme.typography.subheading4,
            color = TDTheme.colors.gray,
        )
        Spacer(Modifier.size(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            TDText(
                text = value,
                style = TDTheme.typography.heading1,
                color = TDTheme.colors.onSurface,
            )
            if (unit.isNotBlank()) {
                Spacer(Modifier.size(4.dp))
                TDText(
                    text = unit,
                    style = TDTheme.typography.heading6,
                    color = TDTheme.colors.gray,
                )
            }
        }
    }
}

@Composable
private fun TotalTimeRow(totalSeconds: Long) {
    val totalMinutes = totalSeconds / 60L
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    val formatted = if (hours > 0L) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(TDTheme.colors.bgColorPurple, RoundedCornerShape(16.dp))
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TDText(
            text = stringResource(R.string.pomodoro_total_time),
            style = TDTheme.typography.heading6,
            color = TDTheme.colors.onBackground,
            modifier = Modifier.weight(1f),
        )
        TDText(
            text = formatted,
            style = TDTheme.typography.heading3,
            color = TDTheme.colors.pendingGray,
        )
    }
}

// region Previews

@TDPreview
@Composable
private fun PomodoroLaunchDefaultsPreview() {
    DoneBotTheme {
        PomodoroLaunchScreen(
            uiState = UiState(isLoading = false),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun PomodoroLaunchCustomPreview() {
    DoneBotTheme {
        PomodoroLaunchScreen(
            uiState = UiState(
                focusMinutes = 50,
                shortBreakMinutes = 10,
                longBreakMinutes = 25,
                sessionCount = 3,
                sectionCount = 2,
                isLoading = false,
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

// endregion
