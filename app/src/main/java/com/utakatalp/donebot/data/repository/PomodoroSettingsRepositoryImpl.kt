package com.utakatalp.donebot.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.utakatalp.donebot.domain.model.Pomodoro
import com.utakatalp.donebot.domain.repository.PomodoroSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PomodoroSettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PomodoroSettingsRepository {

    override fun getSettings(): Flow<Pomodoro?> = dataStore.data.map { prefs ->
        val focus = prefs[FOCUS_TIME] ?: return@map null
        Pomodoro(
            focusTime = focus,
            shortBreak = prefs[SHORT_BREAK] ?: 0,
            longBreak = prefs[LONG_BREAK] ?: 0,
            sessionCount = prefs[SESSION_COUNT] ?: 0,
            sectionCount = prefs[SECTION_COUNT] ?: 0,
        )
    }

    override suspend fun saveSettings(pomodoro: Pomodoro) {
        dataStore.edit { prefs ->
            prefs[FOCUS_TIME] = pomodoro.focusTime
            prefs[SHORT_BREAK] = pomodoro.shortBreak
            prefs[LONG_BREAK] = pomodoro.longBreak
            prefs[SESSION_COUNT] = pomodoro.sessionCount
            prefs[SECTION_COUNT] = pomodoro.sectionCount
        }
    }

    private companion object {
        val FOCUS_TIME = intPreferencesKey("pomodoro_focus_time")
        val SHORT_BREAK = intPreferencesKey("pomodoro_short_break")
        val LONG_BREAK = intPreferencesKey("pomodoro_long_break")
        val SESSION_COUNT = intPreferencesKey("pomodoro_session_count")
        val SECTION_COUNT = intPreferencesKey("pomodoro_section_count")
    }
}
