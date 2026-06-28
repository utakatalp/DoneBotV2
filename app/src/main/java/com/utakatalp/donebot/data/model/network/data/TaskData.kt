package com.utakatalp.donebot.data.model.network.data

import kotlinx.serialization.Serializable

@Serializable
data class TaskData(
    val id: Long,
    val title: String,
    val description: String? = null,
    val date: Long,
    val timeStart: Long,
    val timeEnd: Long,
    val isCompleted: Boolean = false,
    val isSecret: Boolean = false,
)

@Serializable
data class TaskListData(
    val tasks: List<TaskData> = emptyList(),
    val count: Int = 0,
)
