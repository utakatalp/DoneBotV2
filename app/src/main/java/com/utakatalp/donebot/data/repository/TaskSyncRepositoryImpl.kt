package com.utakatalp.donebot.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.utakatalp.donebot.data.sync.FetchTasksWorker
import com.utakatalp.donebot.data.sync.SyncWorker
import com.utakatalp.donebot.domain.repository.TaskSyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class TaskSyncRepositoryImpl @Inject constructor(
    @ApplicationContext context: Context,
) : TaskSyncRepository {

    private val workManager = WorkManager.getInstance(context)

    @Volatile private var lastFetchAt: Long = 0L

    override fun syncPendingTasks() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(networkConstraints())
            .build()
        workManager.beginUniqueWork(SYNC_WORK, ExistingWorkPolicy.REPLACE, request).enqueue()
    }

    override fun fetchTasks(force: Boolean) {
        val withinCooldown = System.currentTimeMillis() - lastFetchAt < FETCH_COOLDOWN_MS
        if (!force && withinCooldown) return
        lastFetchAt = System.currentTimeMillis()

        val constraints = networkConstraints()
        val sync = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()
        val fetch = OneTimeWorkRequestBuilder<FetchTasksWorker>()
            .setConstraints(constraints)
            .build()
        workManager
            .beginUniqueWork(FETCH_WORK, ExistingWorkPolicy.REPLACE, sync)
            .then(fetch)
            .enqueue()
    }

    override fun resetCooldown() {
        lastFetchAt = 0L
    }

    private fun networkConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    companion object {
        private const val SYNC_WORK = "donebot_sync_work"
        private const val FETCH_WORK = "donebot_fetch_work"
        private const val FETCH_COOLDOWN_MS = 60_000L
    }
}
