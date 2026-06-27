package com.utakatalp.donebot.domain.engine

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

enum class PomodoroMode { Focus, ShortBreak, LongBreak }

data class Session(
    val durationSeconds: Long,
    val mode: PomodoroMode,
)

data class PomodoroEngineState(
    val remainingSeconds: Long = 0L,
    val currentSessionTotalSeconds: Long = 0L,
    val mode: PomodoroMode = PomodoroMode.Focus,
    val isRunning: Boolean = false,
    val totalSessions: Int = 0,
    val currentSessionIndex: Int = 0,
)

sealed interface PomodoroEvent {
    data object SessionFinished : PomodoroEvent
    data object PomodoroFinished : PomodoroEvent
}

interface PomodoroEngine {
    val state: StateFlow<PomodoroEngineState>
    val events: SharedFlow<PomodoroEvent>

    /** Replace the session queue and reset progress counters. Does not start the timer. */
    fun setSessionQueue(sessions: List<Session>)

    /** Load the first session of the queue into state without starting the countdown. */
    fun prepare()

    /** Begin (or resume) the countdown for the current session. */
    fun start()

    /** Pause the countdown without losing remaining time. */
    fun pause()

    /** Skip the rest of the current session and advance to the next one. */
    fun skip()

    /** Stop everything, clear the queue, emit [PomodoroEvent.PomodoroFinished]. */
    fun finish()

    /** Stop everything and reset to an idle state without emitting [PomodoroEvent.PomodoroFinished]. */
    fun reset()
}
