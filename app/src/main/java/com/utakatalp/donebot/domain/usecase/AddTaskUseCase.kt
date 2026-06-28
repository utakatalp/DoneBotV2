package com.utakatalp.donebot.domain.usecase

import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.repository.TaskRepository
import javax.inject.Inject

class AddTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val scheduleTaskAlarm: ScheduleTaskAlarmUseCase,
) {
    suspend operator fun invoke(task: Task): Result<Long> {
        val result = taskRepository.addTask(task)
        result.onSuccess { localId -> scheduleTaskAlarm(localId, task) }
        return result
    }
}
