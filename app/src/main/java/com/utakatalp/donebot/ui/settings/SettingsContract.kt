package com.utakatalp.donebot.ui.settings

import androidx.compose.runtime.Immutable

object SettingsContract {

    @Immutable
    data class UiState(
        val leadMinutes: Int = 5,
    )

    sealed interface UiAction {
        data class OnLeadMinutesSelected(val value: Int) : UiAction
    }
}
