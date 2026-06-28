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

    override suspend fun doWork(): Result = taskRepository.syncRemoteTasksWithLocal().fold(
        onSuccess = { Result.success() },
        onFailure = { error ->
            when (error) {
                is DomainException.NoInternet,
                is DomainException.Server,
                -> if (runAttemptCount <= MAX_ATTEMPT) Result.retry() else Result.failure()
                else -> Result.failure()
            }
        },
    )
}
