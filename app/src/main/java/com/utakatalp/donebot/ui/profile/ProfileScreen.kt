package com.utakatalp.donebot.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.todoapp.uikit.components.TDButton
import com.todoapp.uikit.components.TDButtonSize
import com.todoapp.uikit.components.TDButtonType
import com.utakatalp.donebot.R
import com.utakatalp.donebot.ui.profile.ProfileContract.UiAction
import com.utakatalp.donebot.ui.profile.ProfileContract.UiState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun ProfileScreen(
    uiState: StateFlow<UiState>,
    onAction: (UiAction) -> Unit,
) {
    val state by uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (state.isAuthenticated) {
                AuthenticatedProfileBlock(onAction = onAction)
            } else {
                GuestProfileBlock(onAction = onAction)
            }
        }
    }

    if (state.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { onAction(UiAction.OnLogoutDismiss) },
            title = { Text(stringResource(R.string.profile_logout_dialog_title)) },
            text = { Text(stringResource(R.string.profile_logout_dialog_message)) },
            confirmButton = {
                TextButton(onClick = { onAction(UiAction.OnLogoutConfirm) }) {
                    Text(stringResource(R.string.profile_logout_dialog_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { onAction(UiAction.OnLogoutDismiss) }) {
                    Text(stringResource(R.string.profile_logout_dialog_dismiss))
                }
            },
        )
    }
}

@Composable
private fun AuthenticatedProfileBlock(onAction: (UiAction) -> Unit) {
    Text(
        text = stringResource(R.string.profile_authenticated_title),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(32.dp))
    TDButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.profile_logout_button),
        isEnable = true,
        type = TDButtonType.OUTLINE,
        size = TDButtonSize.MEDIUM,
        icon = null,
        onClick = { onAction(UiAction.OnLogoutTap) },
    )
}

@Composable
private fun GuestProfileBlock(onAction: (UiAction) -> Unit) {
    Text(
        text = stringResource(R.string.profile_guest_title),
        style = MaterialTheme.typography.headlineSmall,
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.profile_guest_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(32.dp))
    TDButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.profile_login_button),
        isEnable = true,
        type = TDButtonType.PRIMARY,
        size = TDButtonSize.MEDIUM,
        icon = null,
        onClick = { onAction(UiAction.OnLoginTap) },
    )
    Spacer(Modifier.height(12.dp))
    TDButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(R.string.profile_register_button),
        isEnable = true,
        type = TDButtonType.OUTLINE,
        size = TDButtonSize.MEDIUM,
        icon = null,
        onClick = { onAction(UiAction.OnRegisterTap) },
    )
}
