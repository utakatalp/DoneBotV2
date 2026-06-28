package com.utakatalp.donebot.ui.pomodoro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.engine.PomodoroEngine
import com.utakatalp.donebot.domain.engine.PomodoroEvent
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.ui.pomodoro.PomodoroContract.UiAction
import com.utakatalp.donebot.ui.pomodoro.PomodoroContract.UiEffect
import com.utakatalp.donebot.ui.pomodoro.PomodoroContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroViewModel @Inject constructor(
    private val engine: PomodoroEngine,
) : ViewModel() {

    private val _showFinishDialog = MutableStateFlow(false)

    val uiState: StateFlow<UiState> = combine(
        engine.state,
        _showFinishDialog,
    ) { engineState, showDialog ->
        UiState(
            remainingSeconds = engineState.remainingSeconds,
            currentSessionTotalSeconds = engineState.currentSessionTotalSeconds,
            mode = engineState.mode,
            isRunning = engineState.isRunning,
            currentSessionIndex = engineState.currentSessionIndex,
            totalSessions = engineState.totalSessions,
            showFinishEarlyDialog = showDialog,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            engine.events.collect { event ->
                when (event) {
                    PomodoroEvent.SessionFinished -> emitEffect(UiEffect.SessionFinished)
                    PomodoroEvent.PomodoroFinished -> emitNav(NavigationEffect.GoBack)
                }
            }
        }
    }

    fun onAction(action: UiAction) {
        when (action) {
            UiAction.OnPlayPauseTap -> if (engine.state.value.isRunning) engine.pause() else engine.start()
            UiAction.OnSkipTap -> engine.skip()
            UiAction.OnFinishTap -> _showFinishDialog.value = true
            UiAction.OnDismissFinishDialog -> _showFinishDialog.value = false
            UiAction.OnConfirmFinish -> {
                _showFinishDialog.value = false
                engine.finish()
            }
            UiAction.OnBackTap -> emitNav(NavigationEffect.GoBack)
        }
    }

    private fun emitEffect(effect: UiEffect) = viewModelScope.launch { _uiEffect.send(effect) }
    private fun emitNav(effect: NavigationEffect) = viewModelScope.launch { _navEffect.send(effect) }
}
