package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.domain.model.Pomodoro
import kotlinx.coroutines.flow.Flow

interface PomodoroSettingsRepository {
    fun getSettings(): Flow<Pomodoro?>
    suspend fun saveSettings(pomodoro: Pomodoro)
}
