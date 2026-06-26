package com.utakatalp.donebot.data.source.local.datasource

import com.utakatalp.donebot.data.model.entity.TaskEntity
import com.utakatalp.donebot.data.source.local.TaskDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TaskLocalDataSourceImpl @Inject constructor(
    private val dao: TaskDao
) : TaskLocalDataSource {
    override fun getAllTasks(): Flow<List<TaskEntity>> = dao.getAllTasks()
    override suspend fun getTaskById(id: String): TaskEntity? = dao.getTaskById(id)
    override suspend fun insertTask(task: TaskEntity) = dao.insertTask(task)
    override suspend fun updateTask(task: TaskEntity) = dao.updateTask(task)
    override suspend fun deleteTask(id: String) = dao.deleteTask(id)
}
