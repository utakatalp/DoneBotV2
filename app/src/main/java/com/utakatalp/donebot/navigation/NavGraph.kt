package com.utakatalp.donebot.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.utakatalp.donebot.ui.addtask.AddTaskScreen
import com.utakatalp.donebot.ui.details.DetailsScreen
import com.utakatalp.donebot.ui.home.HomeScreen
import com.utakatalp.donebot.ui.login.LoginScreen
import com.utakatalp.donebot.ui.profile.ProfileScreen
import com.utakatalp.donebot.ui.register.RegisterScreen
import com.utakatalp.donebot.ui.settings.SettingsScreen
import com.utakatalp.donebot.ui.splash.SplashScreen

@Composable
fun AuthNavHost(onAuthenticated: () -> Unit) {
    val backStack = rememberNavBackStack(Splash as NavKey)
    val navigator = remember { AuthNavigator(backStack) }

    NavDisplay(
        backStack = backStack,
        onBack = { navigator.goBack() },
        entryProvider = entryProvider {
            entry<Splash> {
                SplashScreen()
            }
            entry<Login> {
                LoginScreen()
            }
            entry<Register> {
                RegisterScreen()
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
