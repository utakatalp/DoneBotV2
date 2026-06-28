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
class FetchTasksWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val taskRepository: TaskRepository,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        return taskRepository.syncRemoteTasksWithLocal().fold(
            onSuccess = {
                Result.success()
            },
            onFailure = { error ->
                val canRetry = error is DomainException.NoInternet || error is DomainException.Server
                val decision = when {
                    canRetry && runAttemptCount <= MAX_ATTEMPT -> {
                        Result.retry()
                    }
                    canRetry -> {
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
