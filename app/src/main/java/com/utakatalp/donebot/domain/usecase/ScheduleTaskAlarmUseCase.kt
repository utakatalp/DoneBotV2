package com.utakatalp.donebot.domain.usecase

import android.util.Log
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
        Log.d(
            TAG,
            "[ScheduleTaskAlarmUseCase] taskId=$taskId title='${task.title}' " +
                "taskTime=$taskTime lead=${lead}m fireAt=$fireAt now=$now completed=${task.isCompleted}",
        )
        if (fireAt.isAfter(now) && !task.isCompleted) {
            Log.d(TAG, "[ScheduleTaskAlarmUseCase] -> SCHEDULING alarm for taskId=$taskId at $fireAt")
            alarmScheduler.scheduleForTask(
                AlarmItem(
                    fireAt = fireAt,
                    message = task.title,
                    minutesBefore = lead.toLong(),
                    taskId = taskId,
                ),
            )
        } else {
            val reason = when {
                task.isCompleted -> "task is completed"
                !fireAt.isAfter(now) ->
                    "fireAt is in the past (taskTime $taskTime − ${lead}m lead ≤ now $now). " +
                        "Pick a later timeStart or a smaller lead in Settings."
                else -> "unknown"
            }
            Log.d(TAG, "[ScheduleTaskAlarmUseCase] -> CANCELLING alarm for taskId=$taskId reason=$reason")
            alarmScheduler.cancelForTask(taskId)
        }
    }

    private companion object {
        const val TAG = "AlarmFlow"
    }
}
