package com.utakatalp.donebot.ui.login

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.ui.tooling.preview.Preview
import com.todoapp.uikit.previews.TDPreview
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.utakatalp.donebot.ui.login.LoginContract.UiAction
import com.utakatalp.donebot.ui.login.LoginContract.UiEffect
import com.utakatalp.donebot.ui.login.LoginContract.UiState
import kotlinx.coroutines.flow.Flow

@Composable
fun LoginScreen(
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
    LoginContent(uiState = uiState, onAction = onAction)
}

@Composable
private fun LoginContent(uiState: UiState, onAction: (UiAction) -> Unit) {
    val isPortrait = LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT
    if (isPortrait) {
        LoginPortraitContent(uiState = uiState, onAction = onAction)
    } else {
        LoginLandscapeContent(uiState = uiState, onAction = onAction)
    }
}

@Composable
private fun LoginPortraitContent(uiState: UiState, onAction: (UiAction) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Spacer(Modifier.height(32.dp))
        LoginBrandingHeader()
        Spacer(Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 60.dp, topEnd = 60.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(start = 32.dp, end = 32.dp, top = 32.dp),
        ) {
            LoginFormPanel(uiState = uiState, onAction = onAction)
        }
    }
}

@Composable
private fun LoginLandscapeContent(uiState: UiState, onAction: (UiAction) -> Unit) {
    Row(Modifier.fillMaxSize()) {
        LoginBrandingPanel(
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
            LoginFormPanel(uiState = uiState, onAction = onAction)
        }
    }
}

// region Previews

@TDPreview
@Composable
private fun LoginScreenPortraitPreview() {
    DoneBotTheme {
        LoginContent(uiState = UiState(), onAction = {})
    }
}

@TDPreview
@Composable
private fun LoginScreenPortraitFilledPreview() {
    DoneBotTheme {
        LoginContent(
            uiState = UiState(email = "name@example.com", password = "Password123", isPasswordVisible = true),
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
private fun LoginScreenLandscapePreview() {
    DoneBotTheme {
        LoginContent(uiState = UiState(), onAction = {})
    }
}

// endregion
