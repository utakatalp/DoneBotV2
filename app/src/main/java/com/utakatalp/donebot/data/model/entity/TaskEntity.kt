package com.utakatalp.donebot.data.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String = "",
    val title: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val createdAt: Long = 0L
)
