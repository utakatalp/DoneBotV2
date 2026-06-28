package com.utakatalp.donebot.domain.usecase

import com.utakatalp.donebot.domain.repository.TaskRepository
import javax.inject.Inject

class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val cancelTaskAlarm: CancelTaskAlarmUseCase,
) {
    suspend operator fun invoke(taskId: Long): Result<Unit> {
        cancelTaskAlarm(taskId)
        return taskRepository.deleteTask(taskId)
    }
}
