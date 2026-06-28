package com.utakatalp.donebot.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.utakatalp.donebot.domain.repository.ReminderPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ReminderPreferencesImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : ReminderPreferences {

    override fun observeLeadMinutes(): Flow<Int> =
        dataStore.data.map { it[LEAD_MINUTES] ?: DEFAULT_LEAD_MINUTES }

    override suspend fun getLeadMinutes(): Int =
        dataStore.data.first()[LEAD_MINUTES] ?: DEFAULT_LEAD_MINUTES

    override suspend fun setLeadMinutes(value: Int) {
        dataStore.edit { it[LEAD_MINUTES] = value }
    }

    private companion object {
        val LEAD_MINUTES = intPreferencesKey("reminder_lead_minutes")
        const val DEFAULT_LEAD_MINUTES = 5
    }
}
