package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.data.model.network.data.AuthResponseData
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

    suspend fun saveSession(auth: AuthResponseData) {
        setAccessToken(auth.accessToken)
        setRefreshToken(auth.refreshToken)
        setExpiresAt(auth.expiresIn)
    }
}
