package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.data.model.network.data.RefreshTokenData
import com.utakatalp.donebot.data.model.network.request.RefreshTokenRequest

interface AuthRepository {
    suspend fun refresh(request: RefreshTokenRequest): Result<RefreshTokenData>
}
