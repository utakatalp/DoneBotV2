package com.utakatalp.donebot.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.utakatalp.donebot.domain.usecase.SyncPendingTasksUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncPendingTasksUseCaseImpl @Inject constructor(
    @ApplicationContext context: Context,
) : SyncPendingTasksUseCase {

    private val workManager = WorkManager.getInstance(context)

    override operator fun invoke() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints())
            .build()
        workManager.beginUniqueWork(SYNC_WORK, ExistingWorkPolicy.REPLACE, request).enqueue()
    }

    private fun networkConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private companion object {
        const val SYNC_WORK = "donebot_sync_work"
    }
}
