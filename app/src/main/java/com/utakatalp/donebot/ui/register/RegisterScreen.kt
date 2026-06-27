package com.utakatalp.donebot.ui.register

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.todoapp.uikit.previews.TDPreview
import com.utakatalp.donebot.ui.register.RegisterContract.PasswordStrength
import com.utakatalp.donebot.ui.register.RegisterContract.UiAction
import com.utakatalp.donebot.ui.register.RegisterContract.UiEffect
import com.utakatalp.donebot.ui.register.RegisterContract.UiState
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import kotlinx.coroutines.flow.Flow

@Composable
fun RegisterScreen(
    uiState: UiState,
    uiEffect: Flow<UiEffect>,
    onAction: (UiAction) -> Unit,
) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        uiEffect.collect { effect ->
            when (effect) {
                is UiEffect.ShowToast -> Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        RegisterContent(uiState = uiState, onAction = onAction)

        if (uiState.isRedirecting) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.35f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                    ) { },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun RegisterContent(uiState: UiState, onAction: (UiAction) -> Unit) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    if (isPortrait) {
        RegisterPortraitContent(uiState = uiState, onAction = onAction)
    } else {
        RegisterLandscapeContent(uiState = uiState, onAction = onAction)
    }
}

@Composable
private fun RegisterPortraitContent(uiState: UiState, onAction: (UiAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Spacer(Modifier.height(32.dp))
        RegisterBrandingHeader()
        Spacer(Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 32.dp, end = 32.dp, top = 32.dp),
        ) {
            RegisterFormPanel(uiState = uiState, onAction = onAction)
        }
    }
}

@Composable
private fun RegisterLandscapeContent(uiState: UiState, onAction: (UiAction) -> Unit) {
    Row(Modifier.fillMaxSize()) {
        RegisterBrandingPanel(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
        ) {
            RegisterFormPanel(uiState = uiState, onAction = onAction)
        }
    }
}

// region Previews

@TDPreview
@Composable
private fun RegisterContentEmptyPreview() {
    DoneBotTheme {
        RegisterContent(uiState = UiState(), onAction = {})
    }
}

@TDPreview
@Composable
private fun RegisterContentFilledPreview() {
    DoneBotTheme {
        RegisterContent(
            uiState = UiState(
                fullName = "Natalia Smith",
                email = "natalia@example.com",
                password = "Str0ng!Pass#9876",
                confirmPassword = "Str0ng!Pass#9876",
                isPasswordVisible = true,
                passwordStrength = PasswordStrength.STRONG,
            ),
            onAction = {},
        )
    }
}

@TDPreview
@Composable
private fun RegisterContentRedirectingPreview() {
    DoneBotTheme {
        RegisterScreen(
            uiState = UiState(
                fullName = "Natalia Smith",
                email = "natalia@example.com",
                password = "Str0ng!Pass#9876",
                confirmPassword = "Str0ng!Pass#9876",
                isRedirecting = true,
            ),
            uiEffect = kotlinx.coroutines.flow.emptyFlow(),
            onAction = {},
        )
    }
}

@Preview(
    name = "Landscape",
    widthDp = 891,
    heightDp = 411,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Composable
private fun RegisterContentLandscapePreview() {
    DoneBotTheme {
        RegisterContent(uiState = UiState(), onAction = {})
    }
}

// endregion
