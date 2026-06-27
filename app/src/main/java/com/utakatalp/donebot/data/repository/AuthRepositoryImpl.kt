package com.utakatalp.donebot.data.repository

import com.utakatalp.donebot.common.handleRequest
import com.utakatalp.donebot.data.model.network.data.RefreshTokenData
import com.utakatalp.donebot.data.model.network.request.RefreshTokenRequest
import com.utakatalp.donebot.data.source.remote.api.DoneBotAuthApi
import com.utakatalp.donebot.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val authApi: DoneBotAuthApi,
) : AuthRepository {
    override suspend fun refresh(request: RefreshTokenRequest): Result<RefreshTokenData> =
        handleRequest { authApi.refreshToken(request) }
}
