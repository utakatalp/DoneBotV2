package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.data.model.network.data.AuthResponseData
import com.utakatalp.donebot.data.model.network.request.LoginRequest
import com.utakatalp.donebot.data.model.network.request.RegisterRequest

interface UserRepository {
    suspend fun login(request: LoginRequest): Result<AuthResponseData>
    suspend fun register(request: RegisterRequest): Result<AuthResponseData>
}
