package com.utakatalp.donebot.ui.register

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.components.TDButton
import com.todoapp.uikit.previews.TDPreview
import com.utakatalp.donebot.R
import com.utakatalp.donebot.ui.register.RegisterContract.PasswordStrength
import com.utakatalp.donebot.ui.register.RegisterContract.UiAction
import com.utakatalp.donebot.ui.register.RegisterContract.UiState
import com.utakatalp.donebot.ui.theme.DoneBotTheme

@Composable
internal fun RegisterBrandingHeader() {
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
            text = stringResource(R.string.register_header),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.register_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

@Composable
internal fun RegisterBrandingPanel(modifier: Modifier = Modifier) {
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
            text = stringResource(R.string.register_header),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onPrimary,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.register_tagline),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
        )
    }
}

@Composable
internal fun RegisterFormPanel(uiState: UiState, onAction: (UiAction) -> Unit) {
    OutlinedTextField(
        value = uiState.fullName,
        onValueChange = { onAction(UiAction.OnFullNameChange(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.register_full_name_label)) },
        placeholder = { Text(stringResource(R.string.register_full_name_placeholder)) },
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = stringResource(R.string.register_full_name_label),
            )
        },
        shape = RoundedCornerShape(12.dp),
    )

    Spacer(Modifier.height(8.dp))

    OutlinedTextField(
        value = uiState.email,
        onValueChange = { onAction(UiAction.OnEmailChange(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.register_email_label)) },
        placeholder = { Text(stringResource(R.string.register_email_placeholder)) },
        isError = uiState.emailError != null,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = stringResource(R.string.register_email_label),
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

    OutlinedTextField(
        value = uiState.password,
        onValueChange = { onAction(UiAction.OnPasswordChange(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.register_password_label)) },
        placeholder = { Text(stringResource(R.string.register_password_label)) },
        visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = uiState.passwordError != null,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = stringResource(R.string.register_password_label),
            )
        },
        trailingIcon = {
            IconButton(onClick = { onAction(UiAction.OnPasswordVisibilityTap) }) {
                Icon(
                    imageVector = if (uiState.isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = stringResource(R.string.register_toggle_password),
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

    Spacer(Modifier.height(2.dp))

    PasswordStrengthIndicator(strength = uiState.passwordStrength)

    OutlinedTextField(
        value = uiState.confirmPassword,
        onValueChange = { onAction(UiAction.OnConfirmPasswordChange(it)) },
        modifier = Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.register_confirm_password_label)) },
        placeholder = { Text(stringResource(R.string.register_confirm_password_label)) },
        visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        isError = uiState.confirmPasswordError != null,
        singleLine = true,
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = stringResource(R.string.register_confirm_password_label),
            )
        },
        shape = RoundedCornerShape(12.dp),
    )
    uiState.confirmPasswordError?.let {
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
        text = stringResource(R.string.register_sign_up_button),
        isEnable = !uiState.isLoading,
        fullWidth = true,
        onClick = { onAction(UiAction.OnSignUpTap) },
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
            text = stringResource(R.string.register_already_have_account),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Text(
            text = stringResource(R.string.register_login),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { onAction(UiAction.OnLoginTap) },
        )
    }
}

// region Previews

@TDPreview
@Composable
private fun RegisterBrandingHeaderPreview() {
    DoneBotTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            RegisterBrandingHeader()
        }
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
private fun RegisterBrandingPanelPreview() {
    DoneBotTheme {
        RegisterBrandingPanel(modifier = Modifier.fillMaxSize())
    }
}

@TDPreview
@Composable
private fun RegisterFormPanelEmptyPreview() {
    DoneBotTheme {
        RegisterFormPanel(uiState = UiState(), onAction = {})
    }
}

@TDPreview
@Composable
private fun RegisterFormPanelFilledPreview() {
    DoneBotTheme {
        RegisterFormPanel(
            uiState = UiState(
                fullName = "Natalia Smith",
                email = "natalia@example.com",
                password = "Password123",
                confirmPassword = "Password123",
                isPasswordVisible = true,
                passwordStrength = PasswordStrength.MEDIUM,
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun RegisterFormPanelEmailErrorPreview() {
    DoneBotTheme {
        RegisterFormPanel(
            uiState = UiState(
                fullName = "Natalia",
                email = "not-an-email",
                emailError = RegisterContract.RegisterError("Please enter a valid email"),
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun RegisterFormPanelPasswordErrorPreview() {
    DoneBotTheme {
        RegisterFormPanel(
            uiState = UiState(
                fullName = "Natalia",
                email = "natalia@example.com",
                password = "abc",
                passwordError = RegisterContract.RegisterError("Password must be at least 8 characters"),
                passwordStrength = PasswordStrength.WEAK,
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun RegisterFormPanelMismatchPreview() {
    DoneBotTheme {
        RegisterFormPanel(
            uiState = UiState(
                fullName = "Natalia",
                email = "natalia@example.com",
                password = "Password123",
                confirmPassword = "different",
                confirmPasswordError = RegisterContract.RegisterError("Passwords do not match"),
                passwordStrength = PasswordStrength.MEDIUM,
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun RegisterFormPanelGeneralErrorPreview() {
    DoneBotTheme {
        RegisterFormPanel(
            uiState = UiState(
                fullName = "Natalia",
                email = "natalia@example.com",
                password = "Password123",
                confirmPassword = "Password123",
                generalError = RegisterContract.RegisterError("Something went wrong. Please try again."),
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun RegisterFormPanelLoadingPreview() {
    DoneBotTheme {
        RegisterFormPanel(
            uiState = UiState(
                fullName = "Natalia",
                email = "natalia@example.com",
                password = "Password123",
                confirmPassword = "Password123",
                isLoading = true,
                passwordStrength = PasswordStrength.MEDIUM,
            ),
            onAction = {},
        )
    }
}

// endregion
