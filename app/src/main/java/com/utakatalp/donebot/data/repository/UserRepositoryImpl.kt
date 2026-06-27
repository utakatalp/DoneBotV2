package com.utakatalp.donebot.data.repository

import com.utakatalp.donebot.common.handleRequest
import com.utakatalp.donebot.data.model.network.data.AuthResponseData
import com.utakatalp.donebot.data.model.network.request.LoginRequest
import com.utakatalp.donebot.data.model.network.request.RegisterRequest
import com.utakatalp.donebot.data.source.remote.api.DoneBotApi
import com.utakatalp.donebot.domain.repository.UserRepository
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val api: DoneBotApi,
) : UserRepository {

    override suspend fun login(request: LoginRequest): Result<AuthResponseData> =
        handleRequest { api.login(request) }

    override suspend fun register(request: RegisterRequest): Result<AuthResponseData> =
        handleRequest { api.register(request) }
}
