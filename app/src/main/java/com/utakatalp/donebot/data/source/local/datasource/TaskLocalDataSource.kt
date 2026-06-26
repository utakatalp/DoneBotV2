package com.utakatalp.donebot.data.source.local.datasource

import com.utakatalp.donebot.data.model.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

interface TaskLocalDataSource {
    fun getAllTasks(): Flow<List<TaskEntity>>
    suspend fun getTaskById(id: String): TaskEntity?
    suspend fun insertTask(task: TaskEntity)
    suspend fun updateTask(task: TaskEntity)
    suspend fun deleteTask(id: String)
}
