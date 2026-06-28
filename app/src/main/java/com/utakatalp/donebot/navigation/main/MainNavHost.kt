package com.utakatalp.donebot.navigation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.todoapp.uikit.extensions.collectWithLifecycle
import com.utakatalp.donebot.navigation.AddPomodoroTimer
import com.utakatalp.donebot.navigation.AddTask
import com.utakatalp.donebot.navigation.AppKey
import com.utakatalp.donebot.navigation.Details
import com.utakatalp.donebot.navigation.Home
import com.utakatalp.donebot.navigation.Login
import com.utakatalp.donebot.navigation.NavigationEffectController
import com.utakatalp.donebot.navigation.Pomodoro
import com.utakatalp.donebot.navigation.PomodoroLaunch
import com.utakatalp.donebot.navigation.Profile
import com.utakatalp.donebot.navigation.Register
import com.utakatalp.donebot.navigation.Settings
import com.utakatalp.donebot.ui.addtask.AddTaskScreen
import com.utakatalp.donebot.ui.addtask.AddTaskViewModel
import com.utakatalp.donebot.ui.details.DetailsScreen
import com.utakatalp.donebot.ui.home.HomeScreen
import com.utakatalp.donebot.ui.home.HomeViewModel
import com.utakatalp.donebot.ui.pomodoro.PomodoroScreen
import com.utakatalp.donebot.ui.pomodoro.PomodoroViewModel
import com.utakatalp.donebot.ui.pomodoro.banner.PomodoroBanner
import com.utakatalp.donebot.ui.pomodoro.edit.AddPomodoroTimerScreen
import com.utakatalp.donebot.ui.pomodoro.edit.AddPomodoroTimerViewModel
import com.utakatalp.donebot.ui.pomodoro.launch.PomodoroLaunchScreen
import com.utakatalp.donebot.ui.pomodoro.launch.PomodoroLaunchViewModel
import com.utakatalp.donebot.ui.profile.ProfileContract
import com.utakatalp.donebot.ui.profile.ProfileScreen
import com.utakatalp.donebot.ui.profile.ProfileViewModel
import com.utakatalp.donebot.ui.settings.SettingsScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainNavHost(
    onLogout: () -> Unit,
    onRequestAuth: (AppKey) -> Unit,
) {
    val navState = rememberMainNavigationState()
    val navigator = remember(navState) { MainNavigator(navState) }
    val bottomSheetStrategy = remember { BottomSheetSceneStrategy<NavKey>() }

    val entries = navState.toDecoratedEntries(
        entryProvider {
            entry<Home> {
                val viewModel = hiltViewModel<HomeViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NavigationEffectController(
                    navEffect = viewModel.navEffect,
                    onNavigate = { key -> navigator.navigate(key) }
                )
                HomeScreen(
                    uiState = uiState,
                    uiEffect = viewModel.uiEffect,
                    onAction = viewModel::onAction,
                )
            }
            entry<Details> { key ->
                DetailsScreen()
            }
            entry<AddTask>(
                metadata = BottomSheetSceneStrategy.bottomSheet(
                    skipPartiallyExpanded = true,
                )
            ) {
                val viewModel = hiltViewModel<AddTaskViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NavigationEffectController(
                    navEffect = viewModel.navEffect,
                    onNavigate = { key -> navigator.navigate(key) },
                    onBack = { navigator.goBack() },
                )
                AddTaskScreen(
                    uiState = uiState,
                    uiEffect = viewModel.uiEffect,
                    onAction = viewModel::onAction,
                )
            }
            entry<PomodoroLaunch> {
                val viewModel = hiltViewModel<PomodoroLaunchViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NavigationEffectController(
                    navEffect = viewModel.navEffect,
                    onNavigate = { key -> navigator.navigate(key) },
                    onBack = { navigator.goBack() },
                    onReplaceCurrent = { key -> navigator.replaceCurrent(key) },
                )
                PomodoroLaunchScreen(
                    uiState = uiState,
                    uiEffect = viewModel.uiEffect,
                    onAction = viewModel::onAction,
                )
            }
            entry<AddPomodoroTimer> {
                val viewModel = hiltViewModel<AddPomodoroTimerViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NavigationEffectController(
                    navEffect = viewModel.navEffect,
                    onNavigate = { key -> navigator.navigate(key) },
                    onBack = { navigator.goBack() },
                )
                AddPomodoroTimerScreen(
                    uiState = uiState,
                    uiEffect = viewModel.uiEffect,
                    onAction = viewModel::onAction,
                )
            }
            entry<Pomodoro> {
                val viewModel = hiltViewModel<PomodoroViewModel>()
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                NavigationEffectController(
                    navEffect = viewModel.navEffect,
                    onNavigate = { key -> navigator.navigate(key) },
                    onBack = { navigator.goBack() },
                )
                PomodoroScreen(
                    uiState = uiState,
                    uiEffect = viewModel.uiEffect,
                    onAction = viewModel::onAction,
                )
            }
            entry<Profile> {
                val viewModel = hiltViewModel<ProfileViewModel>()
                NavigationEffectController(
                    navEffect = viewModel.navEffect,
                    onNavigate = { key ->
                        when (key) {
                            Login, Register -> onRequestAuth(key)
                            else -> navigator.navigate(key)
                        }
                    },
                )
                viewModel.uiEffect.collectWithLifecycle { effect ->
                    when (effect) {
                        ProfileContract.UiEffect.Logout -> onLogout()
                    }
                }
                ProfileScreen(
                    uiState = viewModel.uiState,
                    onAction = viewModel::onAction,
                )
            }
            entry<Settings> {
                SettingsScreen()
            }
        }
    )

    val currentEntry = navState.backStacks[navState.topLevelRoute]?.lastOrNull()

    Scaffold(
        bottomBar = {
            DoneBotBottomBar(
                currentRoute = navState.topLevelRoute,
                onTabSelected = { navigator.navigate(it) }
            )
        },
        topBar = {
            PomodoroBanner(
                modifier = Modifier.statusBarsPadding(),
                isOnPomodoroScreen = currentEntry == Pomodoro,
                onNavigate = { key -> navigator.navigate(key) },
            )
        }
    ) { innerPadding ->
        NavDisplay(
            modifier = Modifier.padding(innerPadding),
            entries = entries,
            onBack = { navigator.goBack() },
            sceneStrategies = listOf(bottomSheetStrategy)
        )
    }
}
