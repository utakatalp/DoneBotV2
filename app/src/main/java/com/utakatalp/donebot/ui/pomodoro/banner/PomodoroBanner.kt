package com.utakatalp.donebot.ui.pomodoro.banner

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todoapp.uikit.components.TDPomodoroBanner
import com.utakatalp.donebot.navigation.AppKey
import com.utakatalp.donebot.navigation.NavigationEffectController
import com.utakatalp.donebot.ui.pomodoro.components.toColor
import com.utakatalp.donebot.ui.pomodoro.components.toIcon
import com.utakatalp.donebot.ui.pomodoro.components.toLabel

@Composable
fun PomodoroBanner(
    isOnPomodoroScreen: Boolean,
    onNavigate: (AppKey) -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<PomodoroBannerViewModel>()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NavigationEffectController(
        navEffect = viewModel.navEffect,
        onNavigate = onNavigate,
    )
    AnimatedVisibility(
        modifier = modifier,
        visible = uiState.hasActiveSession && !isOnPomodoroScreen,
        enter = slideInVertically { -it } + fadeIn(),
        exit = slideOutVertically { -it } + fadeOut(),
    ) {
        TDPomodoroBanner(
            modeLabel = uiState.mode.toLabel(),
            modeIcon = uiState.mode.toIcon(),
            modeColor = uiState.mode.toColor(),
            timeText = "%02d:%02d".format(uiState.minutes, uiState.seconds),
            isRunning = uiState.isRunning,
            onTap = { viewModel.onAction(PomodoroBannerContract.UiAction.OnTap) },
            onPlayPauseTap = { viewModel.onAction(PomodoroBannerContract.UiAction.OnPlayPauseTap) },
            onSkipTap = { viewModel.onAction(PomodoroBannerContract.UiAction.OnSkipTap) },
        )
    }
}
