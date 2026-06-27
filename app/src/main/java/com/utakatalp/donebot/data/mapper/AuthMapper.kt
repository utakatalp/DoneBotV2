package com.utakatalp.donebot.data.mapper

import com.utakatalp.donebot.data.model.network.data.AuthResponseData
import com.utakatalp.donebot.data.model.network.data.RefreshTokenData
import com.utakatalp.donebot.data.model.network.data.UserData
import com.utakatalp.donebot.domain.model.AuthSession
import com.utakatalp.donebot.domain.model.User

fun AuthResponseData.toDomain(): AuthSession = AuthSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
    user = user.toDomain(),
)

fun RefreshTokenData.toDomain(): AuthSession = AuthSession(
    accessToken = accessToken,
    refreshToken = refreshToken,
    expiresIn = expiresIn,
    user = null,
)

fun UserData.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    avatarUrl = avatarUrl,
    emailVerified = emailVerified,
    providers = providers,
)
