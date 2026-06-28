package com.utakatalp.donebot.navigation

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

/** Drives the auth back stack (Onboarding → Login → Register). */
class AuthNavigator(val backStack: NavBackStack<NavKey>) {
    fun navigate(key: AppKey) = backStack.add(key)
    fun goBack() = backStack.removeLastOrNull()
}

/** Drives the main app back stack with nested per-tab stacks. */
class MainNavigator(val state: NavigationState) {
    fun navigate(key: NavKey) {
        if (key in state.backStacks.keys) {
            // Switch top-level tab
            state.topLevelRoute = key
        } else {
            // Push onto the current tab's stack
            state.backStacks[state.topLevelRoute]?.add(key)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return
        if (currentStack.last() == state.topLevelRoute) {
            // At root of a non-start tab — return to the start tab
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }
}
