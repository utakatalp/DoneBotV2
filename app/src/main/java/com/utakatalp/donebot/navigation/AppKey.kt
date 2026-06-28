package com.utakatalp.donebot.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable sealed interface AppKey : NavKey

// Auth flow
@Serializable data object Onboarding : AppKey
@Serializable data object Login : AppKey
@Serializable data object Register : AppKey

// Bottom-bar tabs (top-level)
@Serializable data object Home : AppKey
@Serializable data object Profile : AppKey

// Home tab sub-routes
@Serializable data class Details(val taskId: Long) : AppKey
@Serializable data object AddTask : AppKey

// Pomodoro
@Serializable data object PomodoroLaunch : AppKey
@Serializable data object Pomodoro : AppKey
@Serializable data object AddPomodoroTimer : AppKey

// Profile tab sub-routes
@Serializable data object Settings : AppKey
