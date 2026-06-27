package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.domain.model.AuthSession

interface AuthRepository {
    suspend fun refresh(refreshToken: String): Result<AuthSession>
}
