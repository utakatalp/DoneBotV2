package com.utakatalp.donebot.domain.usecase

import com.utakatalp.donebot.domain.alarm.AlarmScheduler
import com.utakatalp.donebot.domain.model.AlarmItem
import com.utakatalp.donebot.domain.model.Task
import com.utakatalp.donebot.domain.repository.ReminderPreferences
import java.time.LocalDateTime
import javax.inject.Inject

class ScheduleTaskAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
    private val reminderPreferences: ReminderPreferences,
) {
    suspend operator fun invoke(taskId: Long, task: Task) {
        val lead = reminderPreferences.getLeadMinutes()
        val fireAt = LocalDateTime.of(task.date, task.timeStart).minusMinutes(lead.toLong())
        if (fireAt.isAfter(LocalDateTime.now()) && !task.isCompleted) {
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
