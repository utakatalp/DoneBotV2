package com.utakatalp.donebot.domain.model

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.LocalTime

@Immutable
data class Task(
    val id: Long = 0L,
    val remoteId: Long? = null,
    val title: String,
    val description: String? = null,
    val date: LocalDate,
    val timeStart: LocalTime,
    val timeEnd: LocalTime,
    val isCompleted: Boolean = false,
)
