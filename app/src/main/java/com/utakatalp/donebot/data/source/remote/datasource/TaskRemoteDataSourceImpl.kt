package com.utakatalp.donebot.data.source.remote.datasource

import com.utakatalp.donebot.common.handleRequest
import com.utakatalp.donebot.data.mapper.toCreateRequest
import com.utakatalp.donebot.data.mapper.toUpdateRequest
import com.utakatalp.donebot.data.model.network.data.TaskData
import com.utakatalp.donebot.data.model.network.data.TaskListData
import com.utakatalp.donebot.data.model.network.request.CompleteTaskRequest
import com.utakatalp.donebot.data.source.remote.api.DoneBotApi
import com.utakatalp.donebot.domain.model.Task
import javax.inject.Inject

class TaskRemoteDataSourceImpl @Inject constructor(
    private val api: DoneBotApi,
) : TaskRemoteDataSource {

    override suspend fun getTasks(): Result<TaskListData> =
        handleRequest { api.getTasks() }

    override suspend fun createTask(task: Task): Result<TaskData> =
        handleRequest { api.createTask(task.toCreateRequest()) }

    override suspend fun updateTask(remoteId: Long, task: Task): Result<TaskData> =
        handleRequest { api.updateTask(task.toUpdateRequest(remoteId)) }

    override suspend fun completeTask(remoteId: Long): Result<TaskData> =
        handleRequest { api.completeTask(CompleteTaskRequest(remoteId)) }

    override suspend fun deleteTask(remoteId: Long): Result<Unit> =
        handleRequest { api.deleteTask(remoteId) }
}
