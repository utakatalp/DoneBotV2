package com.utakatalp.donebot.navigation

sealed interface NavigationEffect {
    data class Navigate(val key: AppKey) : NavigationEffect
    data object GoBack : NavigationEffect
}
