package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.domain.model.Pomodoro

interface PomodoroSettingsRepository {
    suspend fun getSettings(): Pomodoro?
    suspend fun saveSettings(pomodoro: Pomodoro)
}
