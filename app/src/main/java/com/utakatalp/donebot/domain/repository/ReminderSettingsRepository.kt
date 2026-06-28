package com.utakatalp.donebot.domain.repository

import kotlinx.coroutines.flow.Flow

interface ReminderSettingsRepository {
    fun observeLeadMinutes(): Flow<Int>
    suspend fun getLeadMinutes(): Int
    suspend fun setLeadMinutes(value: Int)
}
