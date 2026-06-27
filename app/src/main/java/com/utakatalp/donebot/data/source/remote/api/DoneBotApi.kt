package com.utakatalp.donebot.data.source.remote.api

import com.utakatalp.donebot.data.model.network.data.AuthResponseData
import com.utakatalp.donebot.data.model.network.data.RefreshTokenData
import com.utakatalp.donebot.data.model.network.request.LoginRequest
import com.utakatalp.donebot.data.model.network.request.RefreshTokenRequest
import com.utakatalp.donebot.data.model.network.request.RegisterRequest
import com.utakatalp.donebot.data.model.network.response.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface DoneBotApi {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): Response<BaseResponse<AuthResponseData?>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): Response<BaseResponse<AuthResponseData?>>
}

interface DoneBotAuthApi {
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest,
    ): Response<BaseResponse<RefreshTokenData?>>
}
