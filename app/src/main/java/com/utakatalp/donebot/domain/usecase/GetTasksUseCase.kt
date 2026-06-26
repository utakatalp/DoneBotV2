package com.utakatalp.donebot.domain.usecase

import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class GetTasksUseCase(private val repository: TaskRepository) {
    operator fun invoke(): Flow<List<Task>> = repository.getTasks()
}
