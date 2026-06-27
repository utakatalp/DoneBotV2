package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.domain.model.AuthSession

interface UserRepository {
    suspend fun login(email: String, password: String): Result<AuthSession>
    suspend fun register(email: String, password: String, displayName: String): Result<AuthSession>
}
