package com.utakatalp.donebot.data.source.remote.api

import com.utakatalp.donebot.data.model.network.data.AuthResponseData
import com.utakatalp.donebot.data.model.network.data.RefreshTokenData
import com.utakatalp.donebot.data.model.network.data.TaskData
import com.utakatalp.donebot.data.model.network.data.TaskListData
import com.utakatalp.donebot.data.model.network.request.CompleteTaskRequest
import com.utakatalp.donebot.data.model.network.request.CreateTaskRequest
import com.utakatalp.donebot.data.model.network.request.LoginRequest
import com.utakatalp.donebot.data.model.network.request.RefreshTokenRequest
import com.utakatalp.donebot.data.model.network.request.RegisterRequest
import com.utakatalp.donebot.data.model.network.request.UpdateTaskRequest
import com.utakatalp.donebot.data.model.network.response.BaseResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface DoneBotApi {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest,
    ): Response<BaseResponse<AuthResponseData?>>

    @POST("auth/register")
    suspend fun register(
        @Body request: RegisterRequest,
    ): Response<BaseResponse<AuthResponseData?>>

    @GET("tasks")
    suspend fun getTasks(): Response<BaseResponse<TaskListData?>>

    @POST("tasks")
    suspend fun createTask(
        @Body request: CreateTaskRequest,
    ): Response<BaseResponse<TaskData?>>

    @PUT("tasks")
    suspend fun updateTask(
        @Body request: UpdateTaskRequest,
    ): Response<BaseResponse<TaskData?>>

    @PATCH("tasks/complete")
    suspend fun completeTask(
        @Body request: CompleteTaskRequest,
    ): Response<BaseResponse<TaskData?>>

    @DELETE("tasks/{id}")
    suspend fun deleteTask(
        @Path("id") id: Long,
    ): Response<BaseResponse<Unit?>>
}

interface DoneBotAuthApi {
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshTokenRequest,
    ): Response<BaseResponse<RefreshTokenData?>>
}
