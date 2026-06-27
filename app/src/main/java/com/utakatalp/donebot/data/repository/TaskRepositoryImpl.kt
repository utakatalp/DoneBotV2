package com.utakatalp.donebot.data.repository

import com.utakatalp.donebot.common.handleLocal
import com.utakatalp.donebot.data.mapper.toDomain
import com.utakatalp.donebot.data.mapper.toEntity
import com.utakatalp.donebot.data.source.local.datasource.TaskLocalDataSource
import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val localDataSource: TaskLocalDataSource
) : TaskRepository {
    override fun getTasks(): Flow<List<Task>> =
        localDataSource.getAllTasks().map { list -> list.map { it.toDomain() } }

    override suspend fun getTaskById(id: Long): Task? =
        localDataSource.getTaskById(id)?.toDomain()

    override suspend fun addTask(task: Task): Result<Long> =
        handleLocal { localDataSource.insertTask(task.toEntity()) }

    override suspend fun updateTask(task: Task): Result<Unit> =
        handleLocal { localDataSource.updateTask(task.toEntity()) }

    override suspend fun deleteTask(id: Long): Result<Unit> =
        handleLocal { localDataSource.deleteTask(id) }
}
