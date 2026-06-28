package com.utakatalp.donebot.ui.pomodoro.banner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.engine.PomodoroEngine
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.navigation.Pomodoro
import com.utakatalp.donebot.ui.pomodoro.banner.PomodoroBannerContract.UiAction
import com.utakatalp.donebot.ui.pomodoro.banner.PomodoroBannerContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PomodoroBannerViewModel @Inject constructor(
    private val engine: PomodoroEngine,
) : ViewModel() {

    val uiState: StateFlow<UiState> = engine.state
        .map { engineState ->
            UiState(
                mode = engineState.mode,
                remainingSeconds = engineState.remainingSeconds,
                isRunning = engineState.isRunning,
                hasActiveSession = engineState.currentSessionTotalSeconds > 0L,
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    fun onAction(action: UiAction) {
        when (action) {
            UiAction.OnTap -> _navEffect.trySend(NavigationEffect.Navigate(Pomodoro))
            UiAction.OnPlayPauseTap -> if (engine.state.value.isRunning) engine.pause() else engine.start()
            UiAction.OnSkipTap -> engine.skip()
        }
    }
}
