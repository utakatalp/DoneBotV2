package com.utakatalp.donebot.data.model.network.request

import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    val title: String,
    val description: String? = null,
    val date: Long,
    val timeStart: Long,
    val timeEnd: Long,
    val isCompleted: Boolean = false,
)

@Serializable
data class UpdateTaskRequest(
    val id: Long,
    val title: String? = null,
    val description: String? = null,
    val date: Long? = null,
    val timeStart: Long? = null,
    val timeEnd: Long? = null,
    val isCompleted: Boolean? = null,
)

@Serializable
data class CompleteTaskRequest(
    val id: Long,
)
