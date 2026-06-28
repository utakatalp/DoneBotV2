package com.utakatalp.donebot.data.repository

import com.utakatalp.donebot.common.handleRequest
import com.utakatalp.donebot.data.mapper.toDomain
import com.utakatalp.donebot.data.model.network.request.LoginRequest
import com.utakatalp.donebot.data.model.network.request.RefreshTokenRequest
import com.utakatalp.donebot.data.model.network.request.RegisterRequest
import com.utakatalp.donebot.data.source.remote.api.DoneBotAuthApi
import com.utakatalp.donebot.domain.model.AuthSession
import com.utakatalp.donebot.domain.repository.AuthRepository
import javax.inject.Inject

// All endpoints route through DoneBotAuthApi (the no-interceptor, no-authenticator
// Retrofit). This is necessary: TokenRefreshAuthenticator depends on AuthRepository,
// so AuthRepository can't transitively depend on the OkHttpClient that owns the
// authenticator — that would be a DI cycle. It's also correct: login/register/refresh
// never need a Bearer token and never want a 401 to trigger refresh-and-retry.
class AuthRepositoryImpl @Inject constructor(
    private val authApi: DoneBotAuthApi,
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthSession> =
        handleRequest { authApi.login(LoginRequest(email, password)) }.map { it.toDomain() }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
    ): Result<AuthSession> =
        handleRequest { authApi.register(RegisterRequest(email, password, displayName)) }
            .map { it.toDomain() }

    override suspend fun refresh(refreshToken: String): Result<AuthSession> =
        handleRequest { authApi.refreshToken(RefreshTokenRequest(refreshToken)) }
            .map { it.toDomain() }
}
