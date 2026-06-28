package com.utakatalp.donebot.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.utakatalp.donebot.common.DomainException
import com.utakatalp.donebot.domain.repository.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

private const val MAX_ATTEMPT = 2

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val taskRepository: TaskRepository,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return taskRepository.syncLocalTasksToServer().fold(
            onSuccess = {
                Result.success()
            },
            onFailure = { error ->
                // Unauthorized is terminal here: TokenRefreshAuthenticator already tried
                // to refresh inline and gave up before this exception reached us. Retrying
                // would just produce more 401s until the user re-authenticates.
                val canRetry = error is DomainException.NoInternet || error is DomainException.Server
                val decision = when {
                    canRetry && runAttemptCount <= MAX_ATTEMPT -> {
                        Result.retry()
                    }
                    canRetry -> {
                        Result.failure()
                    }
                    error is DomainException.Unauthorized -> {
                        Result.failure()
                    }
                    else -> {
                        Result.failure()
                    }
                }
                decision
            },
        )
    }
}
