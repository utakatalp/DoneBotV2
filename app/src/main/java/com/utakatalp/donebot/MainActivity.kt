package com.utakatalp.donebot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.utakatalp.donebot.navigation.AuthNavHost
import com.utakatalp.donebot.navigation.MainNavHost
import com.utakatalp.donebot.ui.splash.SplashContract.UiAction
import com.utakatalp.donebot.ui.splash.SplashContract.UiState
import com.utakatalp.donebot.ui.splash.SplashScreen
import com.utakatalp.donebot.ui.splash.SplashViewModel
import com.utakatalp.donebot.ui.theme.DoneBotTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DoneBotTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val viewModel: SplashViewModel = hiltViewModel()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val onAction = viewModel::onAction

    when (val current = state) {
        UiState.Resolving -> SplashScreen()
        UiState.EnterApp -> MainNavHost(
            onLogout = { onAction(UiAction.OnLoggedOut) },
            onRequestAuth = { key -> onAction(UiAction.OnRequestAuth(key)) },
        )
        is UiState.NeedsAuth -> AuthNavHost(
            onAuthenticated = { onAction(UiAction.OnAuthenticated) },
            startAt = current.startAt,
            onCancel = if (current.cancelable) {
                { onAction(UiAction.OnCancelAuth) }
            } else null,
        )
    }
}
