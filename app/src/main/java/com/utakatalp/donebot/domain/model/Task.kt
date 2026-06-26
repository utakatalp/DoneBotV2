package com.utakatalp.donebot.domain.model

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = 0L
)
