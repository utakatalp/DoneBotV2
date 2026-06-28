package com.utakatalp.donebot.data.source.remote.datasource

import com.utakatalp.donebot.data.model.network.data.TaskData
import com.utakatalp.donebot.data.model.network.data.TaskListData
import com.utakatalp.donebot.domain.model.Task

interface TaskRemoteDataSource {
    suspend fun getTasks(): Result<TaskListData>
    suspend fun createTask(task: Task): Result<TaskData>
    suspend fun updateTask(remoteId: Long, task: Task): Result<TaskData>
    suspend fun completeTask(remoteId: Long): Result<TaskData>
    suspend fun deleteTask(remoteId: Long): Result<Unit>
}
