package com.utakatalp.donebot.navigation.auth

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.utakatalp.donebot.navigation.AppKey

/** Drives the auth back stack (Onboarding → Login → Register). */
class AuthNavigator(val backStack: NavBackStack<NavKey>) {
    fun navigate(key: AppKey) = backStack.add(key)
    fun goBack() = backStack.removeLastOrNull()
}
