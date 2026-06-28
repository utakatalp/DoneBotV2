package com.utakatalp.donebot.navigation.main

import androidx.navigation3.runtime.NavKey

/** Drives the main app back stack with nested per-tab stacks. */
class MainNavigator(val state: NavigationState) {
    fun navigate(key: NavKey) {
        if (key in state.backStacks.keys) {
            state.topLevelRoute = key
        } else {
            state.backStacks[state.topLevelRoute]?.add(key)
        }
    }

    fun goBack() {
        val currentStack = state.backStacks[state.topLevelRoute] ?: return
        if (currentStack.last() == state.topLevelRoute) {
            state.topLevelRoute = state.startRoute
        } else {
            currentStack.removeLastOrNull()
        }
    }

    /** Pop the current entry and push [key] atomically. Falls back to a normal push when the stack is at its tab root. */
    fun replaceCurrent(key: NavKey) {
        val stack = state.backStacks[state.topLevelRoute] ?: return
        if (stack.size > 1) stack.removeLastOrNull()
        stack.add(key)
    }
}
