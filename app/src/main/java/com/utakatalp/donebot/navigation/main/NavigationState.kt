package com.utakatalp.donebot.navigation.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.utakatalp.donebot.navigation.Home
import com.utakatalp.donebot.navigation.Profile

/** Top-level routes shown in the bottom bar. */
val TOP_LEVEL_ROUTES: Set<NavKey> = setOf(Home, Profile)

@Composable
fun rememberMainNavigationState(): NavigationState {
    val topLevelRouteState = remember { mutableStateOf<NavKey>(Home) }

    // Each tab gets its own persistent back stack.
    val homeStack = rememberNavBackStack(Home as NavKey)
    val profileStack = rememberNavBackStack(Profile as NavKey)

    val backStacks: Map<NavKey, NavBackStack<NavKey>> = remember {
        mapOf(Home to homeStack, Profile to profileStack)
    }

    return remember(homeStack, profileStack) {
        NavigationState(
            startRoute = Home,
            topLevelRoute = topLevelRouteState,
            backStacks = backStacks
        )
    }
}

class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>
) {
    var topLevelRoute: NavKey by topLevelRoute

    /**
     * Decorate each tab's back stack with its own SaveableStateHolder so state is
     * retained independently per tab.
     */
    @Composable
    fun toDecoratedEntries(
        entryProvider: (NavKey) -> NavEntry<NavKey>
    ): List<NavEntry<NavKey>> {
        val decoratedEntries = backStacks.mapValues { (_, stack) ->
            val decorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator<NavKey>(),
                rememberViewModelStoreNavEntryDecorator(),
            )
            rememberDecoratedNavEntries(stack, decorators, entryProvider)
        }
        return getTopLevelRoutesInUse().flatMap { decoratedEntries[it] ?: emptyList() }
    }

    // Always include the start route so the app exits through Home ("exit through home" pattern).
    private fun getTopLevelRoutesInUse(): List<NavKey> =
        if (topLevelRoute == startRoute) listOf(startRoute)
        else listOf(startRoute, topLevelRoute)
}
