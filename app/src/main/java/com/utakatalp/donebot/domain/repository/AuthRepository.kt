package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.domain.model.AuthSession

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun register(email: String, password: String, displayName: String): Result<AuthSession>
    suspend fun refresh(refreshToken: String): Result<AuthSession>
}
