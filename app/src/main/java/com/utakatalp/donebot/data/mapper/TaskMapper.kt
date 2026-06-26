package com.utakatalp.donebot.data.mapper

import com.utakatalp.donebot.data.model.entity.TaskEntity
import com.utakatalp.donebot.domain.model.Task

fun TaskEntity.toDomain() = Task(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    createdAt = createdAt
)

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    isCompleted = isCompleted,
    createdAt = createdAt
)
