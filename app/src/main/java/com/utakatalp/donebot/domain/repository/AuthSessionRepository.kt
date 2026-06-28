package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface AuthSessionRepository {
    suspend fun setAccessToken(token: String)
    suspend fun getAccessToken(): String?

    suspend fun setRefreshToken(token: String)
    suspend fun getRefreshToken(): String?
    fun observeRefreshToken(): Flow<String?>

    suspend fun setExpiresAt(expiresIn: Long)
    suspend fun getExpiresAt(): Long?

    suspend fun clear()

    suspend fun saveSession(session: AuthSession) {
        setAccessToken(session.accessToken)
        setRefreshToken(session.refreshToken)
        setExpiresAt(System.currentTimeMillis() + session.expiresIn * 1000)
    }
}
