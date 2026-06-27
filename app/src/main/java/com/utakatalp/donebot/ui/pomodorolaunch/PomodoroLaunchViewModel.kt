package com.utakatalp.donebot.ui.pomodorolaunch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.engine.PomodoroEngine
import com.utakatalp.donebot.domain.engine.PomodoroMode
import com.utakatalp.donebot.domain.engine.Session
import com.utakatalp.donebot.domain.repository.PomodoroPreferences
import com.utakatalp.donebot.navigation.AddPomodoroTimer
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.navigation.Pomodoro
import com.utakatalp.donebot.ui.pomodorolaunch.PomodoroLaunchContract.UiAction
import com.utakatalp.donebot.ui.pomodorolaunch.PomodoroLaunchContract.UiEffect
import com.utakatalp.donebot.ui.pomodorolaunch.PomodoroLaunchContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PomodoroLaunchViewModel @Inject constructor(
    private val pomodoroPreferences: PomodoroPreferences,
    private val pomodoroEngine: PomodoroEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    init {
        loadSettings()
    }

    fun onAction(action: UiAction) {
        when (action) {
            UiAction.OnStartTap -> startPomodoro()
            UiAction.OnEditTap -> _navEffect.trySend(NavigationEffect.Navigate(AddPomodoroTimer))
            UiAction.OnBackTap -> _navEffect.trySend(NavigationEffect.GoBack)
        }
    }

    private fun loadSettings() = viewModelScope.launch {
        val saved = pomodoroPreferences.getSettings()
        _uiState.update {
            if (saved == null) it.copy(isLoading = false) else it.copy(
                focusMinutes = saved.focusTime,
                shortBreakMinutes = saved.shortBreak,
                longBreakMinutes = saved.longBreak,
                sessionCount = saved.sessionCount,
                sectionCount = saved.sectionCount,
                isLoading = false,
            )
        }
    }

    private fun startPomodoro() {
        val state = _uiState.value
        val queue = buildSessionQueue(state)
        pomodoroEngine.setSessionQueue(queue)
        pomodoroEngine.prepare()
        viewModelScope.launch {
            _navEffect.send(NavigationEffect.Navigate(Pomodoro))
        }
    }

    private fun buildSessionQueue(state: UiState): List<Session> {
        val focusDuration = state.focusMinutes * 60L
        val shortBreakDuration = state.shortBreakMinutes * 60L
        val longBreakDuration = state.longBreakMinutes * 60L
        val sessions = mutableListOf<Session>()
        repeat(state.sectionCount) {
            repeat(state.sessionCount) { sessionIndex ->
                sessions.add(Session(focusDuration, PomodoroMode.Focus))
                if (sessionIndex < state.sessionCount - 1) {
                    sessions.add(Session(shortBreakDuration, PomodoroMode.ShortBreak))
                }
            }
            sessions.add(Session(longBreakDuration, PomodoroMode.LongBreak))
        }
        return sessions
    }
}
