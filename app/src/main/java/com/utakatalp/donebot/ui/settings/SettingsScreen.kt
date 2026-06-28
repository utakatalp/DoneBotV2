package com.utakatalp.donebot.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todoapp.uikit.components.TDText
import com.todoapp.uikit.theme.TDTheme
import com.utakatalp.donebot.R
import com.utakatalp.donebot.ui.settings.SettingsContract.UiAction

private val LEAD_OPTIONS = listOf(0, 5, 10, 15, 30, 60)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    SettingsContent(
        leadMinutes = state.leadMinutes,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun SettingsContent(
    leadMinutes: Int,
    onAction: (UiAction) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TDTheme.colors.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TDText(
            text = stringResource(R.string.settings_title),
            style = TDTheme.typography.heading2,
        )
        TDText(
            text = stringResource(R.string.settings_reminder_lead_label),
            style = TDTheme.typography.heading5,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(TDTheme.colors.surface),
        ) {
            LEAD_OPTIONS.forEach { option ->
                LeadOptionRow(
                    minutes = option,
                    isSelected = option == leadMinutes,
                    onClick = { onAction(UiAction.OnLeadMinutesSelected(option)) },
                )
            }
        }
    }
}

@Composable
private fun LeadOptionRow(
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = isSelected, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = isSelected, onClick = onClick)
        val label = if (minutes == 0) {
            stringResource(R.string.settings_reminder_lead_zero)
        } else {
            stringResource(R.string.settings_reminder_lead_minutes, minutes)
        }
        TDText(text = label, style = TDTheme.typography.regularTextStyle)
    }
}
