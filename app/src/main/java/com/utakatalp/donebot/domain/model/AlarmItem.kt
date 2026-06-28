package com.utakatalp.donebot.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDateTime

@Immutable
data class AlarmItem(
    val fireAt: LocalDateTime,
    val message: String,
    val minutesBefore: Long,
    val taskId: Long,
)
