package com.utakatalp.donebot.data.source.local.datasource

import com.utakatalp.donebot.data.model.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskLocalDataSource {
    fun getAllTasks(): Flow<List<TaskEntity>>
    suspend fun getTaskById(id: Long): TaskEntity?
    suspend fun getTaskByRemoteId(remoteId: Long): TaskEntity?
    suspend fun findPending(): List<TaskEntity>
    suspend fun findSyncedRemoteIds(): List<Long>
    suspend fun insertTask(task: TaskEntity): Long
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(id: Long)
    suspend fun deleteAll()
    suspend fun deleteSyncedByRemoteIds(remoteIds: List<Long>)
}
