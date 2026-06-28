package com.utakatalp.donebot.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.utakatalp.donebot.domain.repository.ReminderSettingsRepository
import com.utakatalp.donebot.domain.usecase.RescheduleAllAlarmsUseCase
import com.utakatalp.donebot.ui.settings.SettingsContract.UiAction
import com.utakatalp.donebot.ui.settings.SettingsContract.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val reminderSettings: ReminderSettingsRepository,
    private val rescheduleAllAlarmsUseCase: RescheduleAllAlarmsUseCase,
) : ViewModel() {

    val uiState: StateFlow<UiState> = reminderSettings.observeLeadMinutes()
        .map { UiState(leadMinutes = it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState())

    fun onAction(action: UiAction) {
        when (action) {
            is UiAction.OnLeadMinutesSelected -> setLeadMinutes(action.value)
        }
    }

    private fun setLeadMinutes(value: Int) = viewModelScope.launch {
        reminderSettings.setLeadMinutes(value)
        rescheduleAllAlarmsUseCase()
    }
}
