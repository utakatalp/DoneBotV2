package com.utakatalp.donebot.data.sync

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.utakatalp.donebot.common.DomainException
import com.utakatalp.donebot.domain.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val MAX_ATTEMPT = 2
private const val TAG = "SyncFlow"

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val taskRepository: TaskRepository,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "[SyncWorker] doWork start attempt=${runAttemptCount + 1}/${MAX_ATTEMPT + 1}")
        return taskRepository.syncLocalTasksToServer().fold(
            onSuccess = {
                Log.d(TAG, "[SyncWorker] doWork SUCCESS — all pending local tasks pushed")
                Result.success()
            },
            onFailure = { error ->
                // Unauthorized is terminal here: TokenRefreshAuthenticator already tried
                // to refresh inline and gave up before this exception reached us. Retrying
                // would just produce more 401s until the user re-authenticates.
                val canRetry = error is DomainException.NoInternet || error is DomainException.Server
                val decision = when {
                    canRetry && runAttemptCount <= MAX_ATTEMPT -> {
                        Log.d(
                            TAG,
                            "[SyncWorker] doWork RETRY (${error::class.simpleName}) " +
                                "attempt=${runAttemptCount + 1}/${MAX_ATTEMPT + 1}",
                        )
                        Result.retry()
                    }
                    canRetry -> {
                        Log.d(
                            TAG,
                            "[SyncWorker] doWork FAILURE — retry budget exhausted " +
                                "(${error::class.simpleName})",
                        )
                        Result.failure()
                    }
                    error is DomainException.Unauthorized -> {
                        Log.d(
                            TAG,
                            "[SyncWorker] doWork FAILURE — Unauthorized (no valid auth; user must sign in)",
                        )
                        Result.failure()
                    }
                    else -> {
                        Log.d(TAG, "[SyncWorker] doWork FAILURE — non-retryable", error)
                        Result.failure()
                    }
                }
                decision
            },
        )
    }
}
