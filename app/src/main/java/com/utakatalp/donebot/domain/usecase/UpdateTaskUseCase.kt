package com.utakatalp.donebot.domain.usecase

import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.repository.TaskRepository
import javax.inject.Inject

class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val scheduleTaskAlarm: ScheduleTaskAlarmUseCase,
) {
    suspend operator fun invoke(task: Task): Result<Unit> {
        val result = taskRepository.updateTask(task)
        result.onSuccess { scheduleTaskAlarm(task.id, task) }
        return result
    }
}
