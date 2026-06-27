package com.utakatalp.donebot.ui.pomodoro

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.components.TDText
import com.todoapp.uikit.previews.TDPreview
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.R
import com.utakatalp.donebot.domain.engine.PomodoroMode
import com.utakatalp.donebot.ui.pomodoro.PomodoroContract.UiAction
import com.utakatalp.donebot.ui.pomodoro.PomodoroContract.UiEffect
import com.utakatalp.donebot.ui.pomodoro.PomodoroContract.UiState
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun PomodoroScreen(
    uiState: UiState,
    uiEffect: Flow<UiEffect>,
    onAction: (UiAction) -> Unit,
) {
    LaunchedEffect(Unit) {
        uiEffect.collect { /* SessionFinished — hook haptic/sound later */ }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TDTheme.colors.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
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
                    text = uiState.mode.toLabel(),
                    style = TDTheme.typography.heading2,
                    color = TDTheme.colors.onBackground,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    imageVector = uiState.mode.toIcon(),
                    contentDescription = null,
                    tint = uiState.mode.toColor(),
                    modifier = Modifier.size(28.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            PomodoroTimerRing(
                progress = uiState.progress,
                mode = uiState.mode,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    TDText(
                        text = "%02d:%02d".format(uiState.minutes, uiState.seconds),
                        style = TDTheme.typography.pomodoro,
                        color = TDTheme.colors.onBackground,
                    )
                    if (uiState.totalSessions > 0) {
                        TDText(
                            text = stringResource(
                                R.string.pomodoro_session_progress,
                                uiState.currentSessionIndex + 1,
                                uiState.totalSessions,
                            ),
                            style = TDTheme.typography.subheading1,
                            color = TDTheme.colors.gray,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            PomodoroSessionDots(
                currentIndex = uiState.currentSessionIndex,
                total = uiState.totalSessions,
                mode = uiState.mode,
            )

            Spacer(Modifier.weight(1f))

            PomodoroControls(
                isRunning = uiState.isRunning,
                onPlayPauseTap = { onAction(UiAction.OnPlayPauseTap) },
                onSkipTap = { onAction(UiAction.OnSkipTap) },
                onFinishTap = { onAction(UiAction.OnFinishTap) },
            )
        }

        if (uiState.showFinishEarlyDialog) {
            AlertDialog(
                onDismissRequest = { onAction(UiAction.OnDismissFinishDialog) },
                containerColor = TDTheme.colors.bgColorPurple,
                title = {
                    TDText(
                        text = stringResource(R.string.pomodoro_finish_dialog_title),
                        style = TDTheme.typography.heading3,
                        color = TDTheme.colors.onSurface,
                    )
                },
                text = {
                    TDText(
                        text = stringResource(R.string.pomodoro_finish_dialog_message),
                        style = TDTheme.typography.heading6,
                        color = TDTheme.colors.gray,
                    )
                },
                confirmButton = {
                    TextButton(onClick = { onAction(UiAction.OnConfirmFinish) }) {
                        TDText(
                            text = stringResource(R.string.pomodoro_finish_dialog_confirm),
                            color = TDTheme.colors.crossRed,
                            style = TDTheme.typography.heading4,
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onAction(UiAction.OnDismissFinishDialog) }) {
                        TDText(
                            text = stringResource(R.string.pomodoro_finish_dialog_dismiss),
                            color = TDTheme.colors.gray,
                            style = TDTheme.typography.heading4,
                        )
                    }
                },
            )
        }
    }
}

// region Previews

@TDPreview
@Composable
private fun PomodoroScreenFocusRunningPreview() {
    DoneBotTheme {
        PomodoroScreen(
            uiState = UiState(
                remainingSeconds = 17 * 60L + 23L,
                currentSessionTotalSeconds = 25 * 60L,
                mode = PomodoroMode.Focus,
                isRunning = true,
                currentSessionIndex = 1,
                totalSessions = 9,
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun PomodoroScreenShortBreakPausedPreview() {
    DoneBotTheme {
        PomodoroScreen(
            uiState = UiState(
                remainingSeconds = 4 * 60L + 15L,
                currentSessionTotalSeconds = 5 * 60L,
                mode = PomodoroMode.ShortBreak,
                isRunning = false,
                currentSessionIndex = 2,
                totalSessions = 9,
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun PomodoroScreenLongBreakPreview() {
    DoneBotTheme {
        PomodoroScreen(
            uiState = UiState(
                remainingSeconds = 14 * 60L + 56L,
                currentSessionTotalSeconds = 15 * 60L,
                mode = PomodoroMode.LongBreak,
                isRunning = true,
                currentSessionIndex = 7,
                totalSessions = 9,
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun PomodoroScreenFinishDialogPreview() {
    DoneBotTheme {
        PomodoroScreen(
            uiState = UiState(
                remainingSeconds = 12 * 60L,
                currentSessionTotalSeconds = 25 * 60L,
                mode = PomodoroMode.Focus,
                isRunning = true,
                currentSessionIndex = 0,
                totalSessions = 4,
                showFinishEarlyDialog = true,
            ),
            uiEffect = emptyFlow(),
            onAction = {},
        )
    }
}

// endregion
