package com.utakatalp.donebot.navigation

sealed interface NavigationEffect {
    data class Navigate(val key: AppKey) : NavigationEffect

    /** Pop the current entry and push [key] in its place — no flicker, no back-stack residue. */
    data class ReplaceCurrent(val key: AppKey) : NavigationEffect

    data object GoBack : NavigationEffect
}
