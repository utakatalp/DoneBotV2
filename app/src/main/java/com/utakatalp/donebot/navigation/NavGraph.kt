package com.utakatalp.donebot.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.todoapp.uikit.components.TDPomodoroBanner
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.utakatalp.donebot.ui.addpomodorotimer.AddPomodoroTimerScreen
import com.utakatalp.donebot.ui.addpomodorotimer.AddPomodoroTimerViewModel
import com.utakatalp.donebot.ui.addtask.AddTaskScreen
import com.utakatalp.donebot.ui.details.DetailsScreen
import com.utakatalp.donebot.ui.home.HomeScreen
import com.utakatalp.donebot.ui.pomodoro.PomodoroBannerContract
import com.utakatalp.donebot.ui.pomodoro.PomodoroBannerViewModel
import com.utakatalp.donebot.ui.pomodoro.PomodoroScreen
import com.utakatalp.donebot.ui.pomodoro.PomodoroViewModel
import com.utakatalp.donebot.ui.pomodoro.toColor
import com.utakatalp.donebot.ui.pomodoro.toIcon
import com.utakatalp.donebot.ui.pomodoro.toLabel
import com.utakatalp.donebot.ui.pomodorolaunch.PomodoroLaunchScreen
import com.utakatalp.donebot.ui.pomodorolaunch.PomodoroLaunchViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.utakatalp.donebot.ui.addtask.AddTaskViewModel
import com.utakatalp.donebot.ui.home.HomeViewModel
import com.utakatalp.donebot.ui.login.LoginScreen
import com.utakatalp.donebot.ui.login.LoginViewModel
import com.utakatalp.donebot.ui.onboarding.OnboardingScreen
import com.utakatalp.donebot.ui.onboarding.OnboardingViewModel
import com.utakatalp.donebot.ui.profile.ProfileScreen
import com.utakatalp.donebot.ui.profile.ProfileViewModel
import com.utakatalp.donebot.ui.register.RegisterScreen
import com.utakatalp.donebot.ui.register.RegisterViewModel
import com.utakatalp.donebot.ui.settings.SettingsScreen

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

    val bannerViewModel = hiltViewModel<PomodoroBannerViewModel>()
    val bannerState by bannerViewModel.uiState.collectAsStateWithLifecycle()
    NavigationEffectController(
        navEffect = bannerViewModel.navEffect,
        onNavigate = { key -> navigator.navigate(key) },
    )
    val currentEntry = navState.backStacks[navState.topLevelRoute]?.lastOrNull()
    val showBanner = bannerState.hasActiveSession && currentEntry != Pomodoro

    Scaffold(
        bottomBar = {
            DoneBotBottomBar(
                currentRoute = navState.topLevelRoute,
                onTabSelected = { navigator.navigate(it) }
            )
        },
        topBar = {
            AnimatedVisibility(
                modifier = Modifier.statusBarsPadding(),
                visible = showBanner,
                enter = slideInVertically { -it } + fadeIn(),
                exit = slideOutVertically { -it } + fadeOut(),
            ) {
                TDPomodoroBanner(
                    modeLabel = bannerState.mode.toLabel(),
                    modeIcon = bannerState.mode.toIcon(),
                    modeColor = bannerState.mode.toColor(),
                    timeText = "%02d:%02d".format(bannerState.minutes, bannerState.seconds),
                    isRunning = bannerState.isRunning,
                    onTap = { bannerViewModel.onAction(PomodoroBannerContract.UiAction.OnTap) },
                    onPlayPauseTap = { bannerViewModel.onAction(PomodoroBannerContract.UiAction.OnPlayPauseTap) },
                    onSkipTap = { bannerViewModel.onAction(PomodoroBannerContract.UiAction.OnSkipTap) },
                )
            }
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
