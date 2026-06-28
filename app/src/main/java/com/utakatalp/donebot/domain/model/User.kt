package com.utakatalp.donebot.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class User(
    val id: Long,
    val email: String,
    val displayName: String?,
    val avatarUrl: String? = null,
    val emailVerified: Boolean = false,
    val providers: List<String> = emptyList(),
)
