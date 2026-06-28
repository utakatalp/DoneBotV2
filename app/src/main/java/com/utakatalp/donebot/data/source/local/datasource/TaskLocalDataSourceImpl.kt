package com.utakatalp.donebot.data.source.local.datasource

import com.utakatalp.donebot.data.model.entity.TaskEntity
import com.utakatalp.donebot.data.source.local.TaskDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskLocalDataSourceImpl @Inject constructor(
    private val dao: TaskDao,
) : TaskLocalDataSource {
    override fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()
    override suspend fun getTaskById(id: Long): TaskEntity? = dao.getTaskById(id)
    override suspend fun getTaskByRemoteId(remoteId: Long): TaskEntity? = dao.getTaskByRemoteId(remoteId)
    override suspend fun findPending(): List<TaskEntity> = dao.findPending()
    override suspend fun findSyncedRemoteIds(): List<Long> = dao.findSyncedRemoteIds()
    override suspend fun insertTask(task: TaskEntity): Long = dao.insertTask(task)
    override suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    override suspend fun deleteTask(id: Long) = dao.deleteTask(id)
    override suspend fun deleteAll() = dao.deleteAll()
    override suspend fun deleteSyncedByRemoteIds(remoteIds: List<Long>) = dao.deleteSyncedByRemoteIds(remoteIds)
}
