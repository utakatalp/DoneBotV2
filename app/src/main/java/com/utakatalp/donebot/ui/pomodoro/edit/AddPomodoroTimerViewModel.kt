package com.utakatalp.donebot.ui.pomodoro.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.model.Pomodoro
import com.utakatalp.donebot.domain.repository.PomodoroSettingsRepository
import com.utakatalp.donebot.navigation.NavigationEffect
import com.utakatalp.donebot.ui.pomodoro.edit.AddPomodoroTimerContract.UiAction
import com.utakatalp.donebot.ui.pomodoro.edit.AddPomodoroTimerContract.UiEffect
import com.utakatalp.donebot.ui.pomodoro.edit.AddPomodoroTimerContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddPomodoroTimerViewModel @Inject constructor(
    private val pomodoroSettings: PomodoroSettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _uiEffect = Channel<UiEffect>(Channel.BUFFERED)
    val uiEffect = _uiEffect.receiveAsFlow()

    private val _navEffect = Channel<NavigationEffect>(Channel.BUFFERED)
    val navEffect = _navEffect.receiveAsFlow()

    init {
        viewModelScope.launch {
            pomodoroSettings.getSettings().first()?.let { saved ->
                _uiState.update {
                    it.copy(
                        focusMinutes = saved.focusTime,
                        shortBreakMinutes = saved.shortBreak,
                        longBreakMinutes = saved.longBreak,
                        sessionCount = saved.sessionCount,
                        sectionCount = saved.sectionCount,
                    )
                }
            }
        }
    }

    fun onAction(action: UiAction) {
        when (action) {
            is UiAction.OnFocusChange -> _uiState.update {
                it.copy(
                    focusMinutes = action.value.coerceIn(
                        AddPomodoroTimerContract.FOCUS_MIN,
                        AddPomodoroTimerContract.FOCUS_MAX,
                    )
                )
            }

            is UiAction.OnShortBreakChange -> _uiState.update {
                it.copy(
                    shortBreakMinutes = action.value.coerceIn(
                        AddPomodoroTimerContract.SHORT_BREAK_MIN,
                        AddPomodoroTimerContract.SHORT_BREAK_MAX,
                    )
                )
            }

            is UiAction.OnLongBreakChange -> _uiState.update {
                it.copy(
                    longBreakMinutes = action.value.coerceIn(
                        AddPomodoroTimerContract.LONG_BREAK_MIN,
                        AddPomodoroTimerContract.LONG_BREAK_MAX,
                    )
                )
            }

            is UiAction.OnSessionCountChange -> _uiState.update {
                it.copy(
                    sessionCount = action.value.coerceIn(
                        AddPomodoroTimerContract.SESSION_COUNT_MIN,
                        AddPomodoroTimerContract.SESSION_COUNT_MAX,
                    )
                )
            }

            is UiAction.OnSectionCountChange -> _uiState.update {
                it.copy(
                    sectionCount = action.value.coerceIn(
                        AddPomodoroTimerContract.SECTION_COUNT_MIN,
                        AddPomodoroTimerContract.SECTION_COUNT_MAX,
                    )
                )
            }

            UiAction.OnSaveTap -> save()
            UiAction.OnBackTap -> emitNav(NavigationEffect.GoBack)
        }
    }

    private fun save() = viewModelScope.launch {
        val state = _uiState.value
        runCatching {
            pomodoroSettings.saveSettings(
                Pomodoro(
                    focusTime = state.focusMinutes,
                    shortBreak = state.shortBreakMinutes,
                    longBreak = state.longBreakMinutes,
                    sessionCount = state.sessionCount,
                    sectionCount = state.sectionCount,
                ),
            )
        }
            .onSuccess { emitNav(NavigationEffect.GoBack) }
            .onFailure { error ->
                emitEffect(UiEffect.ShowError(error.message ?: "Failed to save settings"))
            }
    }

    private fun emitEffect(effect: UiEffect) = viewModelScope.launch { _uiEffect.send(effect) }
    private fun emitNav(effect: NavigationEffect) = viewModelScope.launch { _navEffect.send(effect) }
}
