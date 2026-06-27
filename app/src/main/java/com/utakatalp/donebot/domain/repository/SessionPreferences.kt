package com.utakatalp.donebot.domain.repository

import kotlinx.coroutines.flow.Flow

interface SessionPreferences {
    suspend fun setAccessToken(token: String)
    suspend fun getAccessToken(): String?

    suspend fun setRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    fun observeRefreshToken(): Flow<String?>

    suspend fun setExpiresAt(expiresIn: Long)
    suspend fun getExpiresAt(): Long?

    suspend fun clear()
}
