package com.utakatalp.donebot.domain.usecase

import com.utakatalp.donebot.domain.alarm.AlarmScheduler
import javax.inject.Inject

class CancelTaskAlarmUseCase @Inject constructor(
    private val alarmScheduler: AlarmScheduler,
) {
    operator fun invoke(taskId: Long) {
        alarmScheduler.cancelForTask(taskId)
    }
}
