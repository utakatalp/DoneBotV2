package com.utakatalp.donebot.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.utakatalp.donebot.domain.usecase.FetchTasksUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FetchTasksUseCaseImpl @Inject constructor(
    @ApplicationContext context: Context,
) : FetchTasksUseCase {

    private val workManager = WorkManager.getInstance(context)

    // Cooldown state lives in this @Singleton impl rather than the interface. Google's
    // domain-layer guidance says use cases shouldn't carry mutable data, but a debounce
    // timer is an implementation detail of the scheduling itself, not user-facing state.
    @Volatile private var lastFetchAt: Long = 0L

    override operator fun invoke(force: Boolean) {
        val now = System.currentTimeMillis()
        val sinceLast = now - lastFetchAt
        val withinCooldown = sinceLast < FETCH_COOLDOWN_MS
        if (!force && withinCooldown) {
            return
        }
        lastFetchAt = now

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

    private fun networkConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    private companion object {
        const val FETCH_WORK = "donebot_fetch_work"
        const val FETCH_COOLDOWN_MS = 60_000L
    }
}
