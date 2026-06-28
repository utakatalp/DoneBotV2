package com.utakatalp.donebot.domain.usecase

import com.utakatalp.donebot.domain.alarm.AlarmScheduler
import com.utakatalp.donebot.domain.model.AlarmItem
import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.repository.ReminderSettingsRepository
import java.time.LocalDateTime
import javax.inject.Inject

class ScheduleTaskAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val reminderSettings: ReminderSettingsRepository,
) {
    suspend operator fun invoke(taskId: Long, task: Task) {
        val lead = reminderSettings.getLeadMinutes()
        val taskTime = LocalDateTime.of(task.date, task.timeStart)
        val fireAt = taskTime.minusMinutes(lead.toLong())
        val now = LocalDateTime.now()
        if (fireAt.isAfter(now) && !task.isCompleted) {
            alarmScheduler.scheduleForTask(
                AlarmItem(
                    fireAt = fireAt,
                    message = task.title,
                    minutesBefore = lead.toLong(),
                    taskId = taskId,
                ),
            )
        } else {
            alarmScheduler.cancelForTask(taskId)
        }
    }
}
