package com.utakatalp.donebot.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: User? = null,
)
