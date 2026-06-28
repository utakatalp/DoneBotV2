package com.utakatalp.donebot.domain.alarm

import com.utakatalp.donebot.domain.model.AlarmItem

interface AlarmScheduler {
    fun scheduleForTask(item: AlarmItem)
    fun cancelForTask(taskId: Long)
}
