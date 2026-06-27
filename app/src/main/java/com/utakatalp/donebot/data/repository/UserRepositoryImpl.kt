package com.utakatalp.donebot.data.repository

import com.utakatalp.donebot.common.handleRequest
import com.utakatalp.donebot.data.mapper.toDomain
import com.utakatalp.donebot.data.model.network.request.LoginRequest
import com.utakatalp.donebot.data.model.network.request.RegisterRequest
import com.utakatalp.donebot.data.source.remote.api.DoneBotApi
import com.utakatalp.donebot.domain.model.AuthSession
import com.utakatalp.donebot.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: DoneBotApi,
) : UserRepository {

    override suspend fun login(email: String, password: String): Result<AuthSession> =
        handleRequest { api.login(LoginRequest(email, password)) }.map { it.toDomain() }

    override suspend fun register(
        email: String,
        password: String,
        displayName: String,
    ): Result<AuthSession> =
        handleRequest { api.register(RegisterRequest(email, password, displayName)) }
            .map { it.toDomain() }
}
