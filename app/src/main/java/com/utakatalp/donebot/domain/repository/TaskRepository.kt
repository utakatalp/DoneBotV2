package com.utakatalp.donebot.domain.repository

import com.utakatalp.donebot.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getTasks(): Flow<List<Task>>
    suspend fun getTaskById(id: Long): Task?
    suspend fun addTask(task: Task): Result<Long>
    suspend fun updateTask(task: Task): Result<Unit>
    suspend fun deleteTask(id: Long): Result<Unit>
}
