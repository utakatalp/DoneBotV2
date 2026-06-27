package com.utakatalp.donebot.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.utakatalp.donebot.ui.addtask.AddTaskScreen
import com.utakatalp.donebot.ui.details.DetailsScreen
import com.utakatalp.donebot.ui.home.HomeScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.utakatalp.donebot.ui.login.LoginScreen
import com.utakatalp.donebot.ui.login.LoginViewModel
import com.utakatalp.donebot.ui.onboarding.OnboardingScreen
import com.utakatalp.donebot.ui.onboarding.OnboardingViewModel
import com.utakatalp.donebot.ui.profile.ProfileScreen
import com.utakatalp.donebot.ui.register.RegisterScreen
import com.utakatalp.donebot.ui.register.RegisterViewModel
import com.utakatalp.donebot.ui.settings.SettingsScreen
import com.utakatalp.donebot.ui.splash.SplashScreen

@Composable
fun AuthNavHost(onAuthenticated: () -> Unit) {
    val backStack = rememberNavBackStack(Onboarding as NavKey)
    val navigator = remember { AuthNavigator(backStack) }

    NavDisplay(
        backStack = backStack,
        onBack = { navigator.goBack() },
        entryProvider = entryProvider {
            entry<Splash> {
                SplashScreen()
            }
            entry<Onboarding> {
                val viewModel = hiltViewModel<OnboardingViewModel>()
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
                    uiState = viewModel.uiState,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavHost(onLogout: () -> Unit) {
    val navState = rememberMainNavigationState()
    val navigator = remember(navState) { MainNavigator(navState) }
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }

    val entries = navState.toDecoratedEntries(
        entryProvider {
            entry<Home> {
                HomeScreen()
            }
            entry<Details> { key ->
                DetailsScreen()
            }
            entry<AddTask>(
                metadata = BottomSheetSceneStrategy.bottomSheet()
            ) {
                AddTaskScreen()
            }
            entry<Profile> {
                ProfileScreen()
            }
            entry<Settings> {
                SettingsScreen()
            }
        }
    )

    Scaffold(
        bottomBar = {
            DoneBotBottomBar(
                currentRoute = navState.topLevelRoute,
                onTabSelected = { navigator.navigate(it) }
            )
        }
    ) { _ ->
        NavDisplay(
            entries = entries,
            onBack = { navigator.goBack() },
            sceneStrategies = listOf(bottomSheetStrategy)
        )
    }
}
