package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.domain.model.Pomodoro

interface PomodoroPreferences {
    suspend fun getSettings(): Pomodoro?
    suspend fun saveSettings(pomodoro: Pomodoro)
}
