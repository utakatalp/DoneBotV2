package com.utakatalp.donebot.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Pomodoro(
    val focusTime: Int,
    val shortBreak: Int,
    val longBreak: Int,
    val sessionCount: Int,
    val sectionCount: Int,
)
