package com.utakatalp.donebot.data.model.network.data

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserData,
)

@Serializable
data class UserData(
    val id: Long,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val emailVerified: Boolean = false,
    val providers: List<String> = emptyList(),
    val createdAt: String = "",
)

@Serializable
data class RefreshTokenData(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)
