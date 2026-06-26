package com.utakatalp.donebot.ui.login

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.todoapp.uikit.components.TDButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.todoapp.uikit.previews.TDPreview
import com.utakatalp.donebot.R
import com.utakatalp.donebot.ui.login.LoginContract.UiAction
import com.utakatalp.donebot.ui.login.LoginContract.UiState
import com.utakatalp.donebot.ui.theme.DoneBotTheme

@Composable
internal fun LoginBrandingHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(70.dp)
                .background(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.25f),
                    shape = RoundedCornerShape(16.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_text),
                contentDescription = null,
                modifier = Modifier.size(40.dp),
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.login_header),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Text(
            text = stringResource(R.string.login_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
        )
    }
}

@Composable
internal fun LoginBrandingPanel(modifier: Modifier = Modifier) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
        ),
    )
    Column(
        modifier = modifier
            .background(gradient)
            .statusBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_text),
            contentDescription = null,
            modifier = Modifier.size(160.dp),
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.login_header),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.login_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        )
    }
}

@Composable
internal fun LoginFormPanel(uiState: UiState, onAction: (UiAction) -> Unit) {
    Text(
        text = stringResource(R.string.login_welcome_back),
        style = MaterialTheme.typography.headlineMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Spacer(Modifier.height(4.dp))
    Text(
        text = stringResource(R.string.login_sign_in_prompt),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(Modifier.height(24.dp))

    OutlinedTextField(
        value = uiState.email,
        onValueChange = { onAction(UiAction.OnEmailChange(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.login_email_label)) },
        placeholder = { Text(stringResource(R.string.login_email_placeholder)) },
        isError = uiState.emailError != null,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = stringResource(R.string.login_email_placeholder),
            )
        },
        shape = RoundedCornerShape(12.dp),
    )
    uiState.emailError?.let {
        Text(
            text = it.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }

    Spacer(Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.login_password_label),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = stringResource(R.string.login_forgot_password),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onAction(UiAction.OnForgotPasswordTap) },
        )
    }
    Spacer(Modifier.height(4.dp))

    OutlinedTextField(
        value = uiState.password,
        onValueChange = { onAction(UiAction.OnPasswordChange(it)) },
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(stringResource(R.string.login_password_label)) },
        visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = uiState.passwordError != null,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = stringResource(R.string.login_password_label),
            )
        },
        trailingIcon = {
            IconButton(onClick = { onAction(UiAction.OnPasswordVisibilityTap) }) {
                Icon(
                    imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = stringResource(R.string.login_toggle_password),
                )
            }
        },
        shape = RoundedCornerShape(12.dp),
    )
    uiState.passwordError?.let {
        Text(
            text = it.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }

    uiState.generalError?.let {
        Spacer(Modifier.height(8.dp))
        Text(
            text = it.message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
        )
    }

    Spacer(Modifier.height(24.dp))

    TDButton(
        text = stringResource(R.string.login_button),
        isEnable = !uiState.isLoading,
        fullWidth = true,
        onClick = { onAction(UiAction.OnLoginTap) },
    )

    Spacer(Modifier.height(24.dp))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.login_no_account),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Text(
            text = stringResource(R.string.login_register),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onAction(UiAction.OnRegisterTap) },
        )
    }
}

// region Previews

@TDPreview
@Composable
private fun LoginBrandingHeaderPreview() {
    DoneBotTheme {
        LoginBrandingHeader()
    }
}

@Preview(
    name = "Landscape",
    widthDp = 480,
    heightDp = 360,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun LoginBrandingPanelPreview() {
    DoneBotTheme {
        LoginBrandingPanel(modifier = Modifier.fillMaxSize())
    }
}

@TDPreview
@Composable
private fun LoginFormPanelEmptyPreview() {
    DoneBotTheme {
        LoginFormPanel(uiState = UiState(), onAction = {})
    }
}

@TDPreview
@Composable
private fun LoginFormPanelFilledPreview() {
    DoneBotTheme {
        LoginFormPanel(
            uiState = UiState(email = "name@example.com", password = "Password123", isPasswordVisible = true),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun LoginFormPanelEmailErrorPreview() {
    DoneBotTheme {
        LoginFormPanel(
            uiState = UiState(
                email = "not-an-email",
                emailError = LoginContract.LoginError("Please enter a valid email"),
                hasSubmittedOnce = true,
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun LoginFormPanelPasswordErrorPreview() {
    DoneBotTheme {
        LoginFormPanel(
            uiState = UiState(
                email = "name@example.com",
                password = "abc",
                passwordError = LoginContract.LoginError("Password must be at least 8 characters"),
                hasSubmittedOnce = true,
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun LoginFormPanelGeneralErrorPreview() {
    DoneBotTheme {
        LoginFormPanel(
            uiState = UiState(
                email = "name@example.com",
                password = "Password123",
                generalError = LoginContract.LoginError("Invalid email or password"),
                hasSubmittedOnce = true,
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun LoginFormPanelLoadingPreview() {
    DoneBotTheme {
        LoginFormPanel(
            uiState = UiState(email = "name@example.com", password = "Password123", isLoading = true),
            onAction = {},
        )
    }
}

// endregion
