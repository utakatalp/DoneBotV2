package com.utakatalp.donebot.domain.usecase

import com.utakatalp.donebot.domain.repository.TaskRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class RescheduleAllAlarmsUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val scheduleTaskAlarm: ScheduleTaskAlarmUseCase,
) {
    suspend operator fun invoke() {
        val tasks = taskRepository.getTasks().first()
        for (task in tasks) {
            scheduleTaskAlarm(task.id, task)
        }
    }
}
