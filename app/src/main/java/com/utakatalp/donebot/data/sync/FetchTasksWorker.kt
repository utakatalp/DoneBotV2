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
class FetchTasksWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val taskRepository: TaskRepository,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "[FetchTasksWorker] doWork start attempt=${runAttemptCount + 1}/${MAX_ATTEMPT + 1}")
        return taskRepository.syncRemoteTasksWithLocal().fold(
            onSuccess = {
                Log.d(TAG, "[FetchTasksWorker] doWork SUCCESS — remote tasks merged into local")
                Result.success()
            },
            onFailure = { error ->
                val canRetry = error is DomainException.NoInternet || error is DomainException.Server
                val decision = when {
                    canRetry && runAttemptCount <= MAX_ATTEMPT -> {
                        Log.d(
                            TAG,
                            "[FetchTasksWorker] doWork RETRY (${error::class.simpleName}) " +
                                "attempt=${runAttemptCount + 1}/${MAX_ATTEMPT + 1}",
                        )
                        Result.retry()
                    }
                    canRetry -> {
                        Log.d(
                            TAG,
                            "[FetchTasksWorker] doWork FAILURE — retry budget exhausted " +
                                "(${error::class.simpleName})",
                        )
                        Result.failure()
                    }
                    else -> {
                        Log.d(TAG, "[FetchTasksWorker] doWork FAILURE — non-retryable", error)
                        Result.failure()
                    }
                }
                decision
            },
        )
    }
}
