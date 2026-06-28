package com.utakatalp.donebot.navigation.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.utakatalp.donebot.navigation.AppKey
import com.utakatalp.donebot.navigation.Home
import com.utakatalp.donebot.navigation.Login
import com.utakatalp.donebot.navigation.NavigationEffectController
import com.utakatalp.donebot.navigation.Onboarding
import com.utakatalp.donebot.navigation.Register
import com.utakatalp.donebot.ui.login.LoginScreen
import com.utakatalp.donebot.ui.login.LoginViewModel
import com.utakatalp.donebot.ui.onboarding.OnboardingScreen
import com.utakatalp.donebot.ui.onboarding.OnboardingViewModel
import com.utakatalp.donebot.ui.register.RegisterScreen
import com.utakatalp.donebot.ui.register.RegisterViewModel

@Composable
fun AuthNavHost(
    onAuthenticated: () -> Unit,
    startAt: AppKey = Onboarding,
    onCancel: (() -> Unit)? = null,
) {
    val backStack = rememberNavBackStack(startAt as NavKey)
    val navigator = remember { AuthNavigator(backStack) }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size <= 1 && onCancel != null) onCancel()
            else navigator.goBack()
        },
        entryProvider = entryProvider {
            entry<Onboarding> {
                val viewModel = hiltViewModel<OnboardingViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NavigationEffectController(
                    navEffect = viewModel.navEffect,
                    onNavigate = { key ->
                        when (key) {
                            Home -> onAuthenticated()
                            else -> navigator.navigate(key)
                        }
                    }
                )
                OnboardingScreen(
                    uiState = uiState,
                    onAction = viewModel::onAction,
                )
            }
            entry<Login> {
                val viewModel = hiltViewModel<LoginViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NavigationEffectController(
                    navEffect = viewModel.navEffect,
                    onNavigate = { key ->
                        when (key) {
                            Home -> onAuthenticated()
                            else -> navigator.navigate(key)
                        }
                    }
                )
                LoginScreen(
                    uiState = uiState,
                    uiEffect = viewModel.uiEffect,
                    onAction = viewModel::onAction,
                )
            }
            entry<Register> {
                val viewModel = hiltViewModel<RegisterViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NavigationEffectController(
                    navEffect = viewModel.navEffect,
                    onNavigate = { key ->
                        when (key) {
                            Home -> onAuthenticated()
                            else -> navigator.navigate(key)
                        }
                    }
                )
                RegisterScreen(
                    uiState = uiState,
                    uiEffect = viewModel.uiEffect,
                    onAction = viewModel::onAction,
                )
            }
        }
    )
}
