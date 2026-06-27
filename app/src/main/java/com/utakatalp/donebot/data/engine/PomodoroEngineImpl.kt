package com.utakatalp.donebot.data.engine

import android.util.Log
import com.utakatalp.donebot.domain.engine.PomodoroEngine
import com.utakatalp.donebot.domain.engine.PomodoroEngineState
import com.utakatalp.donebot.domain.engine.PomodoroEvent
import com.utakatalp.donebot.domain.engine.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.onTimeout
import kotlinx.coroutines.selects.select
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

private const val TICK_MILLIS = 1_000L
private const val TAG = "PomodoroEngine"

@Singleton
class PomodoroEngineImpl @Inject constructor() : PomodoroEngine {

    private val _state = MutableStateFlow(PomodoroEngineState())
    override val state: StateFlow<PomodoroEngineState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PomodoroEvent>(extraBufferCapacity = 16)
    override val events: SharedFlow<PomodoroEvent> = _events.asSharedFlow()

    private val commands: Channel<Command> = Channel(Channel.UNLIMITED)

    /** Confined to the actor coroutine — never touched by any other context. */
    private val queue: ArrayDeque<Session> = ArrayDeque()

    /**
     * Lazily created scope owning the actor coroutine. Null when the engine is idle
     * (no session running, nothing queued). `setSessionQueue` creates it; the actor
     * tears it down from inside after the queue exhausts or on Finish / Reset.
     */
    @Volatile
    private var actorScope: CoroutineScope? = null
    private val lifecycleLock = Any()

    override fun setSessionQueue(sessions: List<Session>) {
        ensureActorAlive()
        commands.trySend(Command.SetQueue(sessions))
    }

    override fun prepare() {
        if (actorScope == null) return
        commands.trySend(Command.Prepare)
    }

    override fun start() {
        if (actorScope == null) return
        commands.trySend(Command.Start)
    }

    override fun pause() {
        if (actorScope == null) return
        commands.trySend(Command.Pause)
    }

    override fun skip() {
        if (actorScope == null) return
        commands.trySend(Command.Skip)
    }

    override fun finish() {
        if (actorScope == null) return
        commands.trySend(Command.Finish)
    }

    override fun reset() {
        if (actorScope == null) return
        commands.trySend(Command.Reset)
    }

    private fun ensureActorAlive() {
        synchronized(lifecycleLock) {
            if (actorScope != null) return
            val s = CoroutineScope(SupervisorJob() + Dispatchers.Default)
            actorScope = s
            s.launch { runActor() }
        }
    }

    /**
     * Cancel the actor's scope and clear the reference. Called from inside the actor
     * coroutine after a Pomodoro completes. The actor's `while(isActive)` check sees
     * false on the next iteration and the coroutine exits cleanly.
     */
    private fun teardown() {
        synchronized(lifecycleLock) {
            actorScope?.cancel()
            actorScope = null
        }
    }

    /**
     * Single-threaded event loop. When `isRunning`, races a 1-second tick against the
     * incoming command channel — whichever wins gets exclusive ownership of the queue
     * and state for that step. When not running, blocks on the channel until something
     * arrives. Either way, queue and state mutations happen in exactly one coroutine.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private suspend fun runActor() {
        while (currentCoroutineContext().isActive) {
            if (_state.value.isRunning && queue.isNotEmpty()) {
                select<Unit> {
                    onTimeout(TICK_MILLIS.milliseconds) { handleTick() }
                    commands.onReceive { cmd -> handleCommand(cmd) }
                }
            } else {
                handleCommand(commands.receive())
            }
        }
    }

    private suspend fun handleTick() {
        val newRemaining = (_state.value.remainingSeconds - 1L).coerceAtLeast(0L)
        if (newRemaining > 0L) {
            _state.update { it.copy(remainingSeconds = newRemaining) }
        } else {
            _state.update { it.copy(remainingSeconds = 0L) }
            advanceToNextSession()
        }
    }

    private suspend fun handleCommand(cmd: Command) {
        when (cmd) {
            is Command.SetQueue -> {
                queue.clear()
                queue.addAll(cmd.sessions)
                _state.update { PomodoroEngineState(totalSessions = cmd.sessions.size) }
            }
            Command.Prepare -> {
                val first = queue.firstOrNull() ?: return
                _state.update {
                    it.copy(
                        remainingSeconds = first.durationSeconds,
                        currentSessionTotalSeconds = first.durationSeconds,
                        mode = first.mode,
                        isRunning = false,
                        currentSessionIndex = 0,
                    )
                }
            }
            Command.Start -> {
                if (queue.isEmpty()) return
                _state.update { it.copy(isRunning = true) }
            }
            Command.Pause -> {
                _state.update { it.copy(isRunning = false) }
            }
            Command.Skip -> {
                if (queue.isEmpty()) return
                advanceToNextSession()
            }
            Command.Finish -> {
                queue.clear()
                _state.update { PomodoroEngineState() }
                _events.emit(PomodoroEvent.PomodoroFinished)
                teardown()
            }
            Command.Reset -> {
                queue.clear()
                _state.update { PomodoroEngineState() }
                teardown()
            }
        }
    }

    /** Pop the current session, emit events, load the next one or shut the actor down. Actor-only. */
    private suspend fun advanceToNextSession(): Boolean {
        Log.d(
            TAG,
            "advanceToNextSession: currentIndex=${_state.value.currentSessionIndex}, " +
                "remaining=${_state.value.remainingSeconds}, queueSize=${queue.size}",
        )
        _events.emit(PomodoroEvent.SessionFinished)
        queue.removeFirstOrNull()
        val next = queue.firstOrNull()
        Log.d(TAG, "advanceToNextSession: removed current session, next=$next, queueSize=${queue.size}")
        if (next == null) {
            Log.d(TAG, "advanceToNextSession: no next session, pomodoro finished")
            _events.emit(PomodoroEvent.PomodoroFinished)
            _state.update { PomodoroEngineState() }
            queue.clear()
            teardown()
            return false
        }
        Log.d(
            TAG,
            "advanceToNextSession: loading next session mode=${next.mode}, duration=${next.durationSeconds}",
        )
        _state.update {
            it.copy(
                remainingSeconds = next.durationSeconds,
                currentSessionTotalSeconds = next.durationSeconds,
                mode = next.mode,
                currentSessionIndex = it.currentSessionIndex + 1,
            )
        }
        return true
    }

    private sealed interface Command {
        data class SetQueue(val sessions: List<Session>) : Command
        data object Prepare : Command
        data object Start : Command
        data object Pause : Command
        data object Skip : Command
        data object Finish : Command
        data object Reset : Command
    }
}
