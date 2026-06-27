package com.utakatalp.donebot.data.mapper

import com.utakatalp.donebot.data.model.entity.SyncStatus
import com.utakatalp.donebot.data.model.entity.TaskEntity
import com.utakatalp.donebot.domain.model.Task
import java.time.LocalDate
import java.time.LocalTime

fun TaskEntity.toDomain(): Task = Task(
    id = id,
    remoteId = remoteId,
    title = title,
    description = description,
    date = LocalDate.ofEpochDay(date),
    timeStart = LocalTime.of((timeStart / 60).toInt(), (timeStart % 60).toInt()),
    timeEnd = LocalTime.of((timeEnd / 60).toInt(), (timeEnd % 60).toInt()),
    isCompleted = isCompleted,
)

fun Task.toEntity(
    syncStatus: SyncStatus = SyncStatus.PENDING_CREATE,
    orderIndex: Int = 0,
): TaskEntity = TaskEntity(
    id = id,
    remoteId = remoteId,
    title = title,
    description = description,
    date = date.toEpochDay(),
    timeStart = (timeStart.hour * 60 + timeStart.minute).toLong(),
    timeEnd = (timeEnd.hour * 60 + timeEnd.minute).toLong(),
    isCompleted = isCompleted,
    syncStatus = syncStatus,
    orderIndex = orderIndex,
)
