package com.utakatalp.donebot

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.utakatalp.donebot.di.ApplicationScope
import com.utakatalp.donebot.domain.usecase.FetchTasksUseCase
import com.utakatalp.donebot.domain.usecase.RescheduleAllAlarmsUseCase
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class DoneBotApplication : Application(), Configuration.Provider, DefaultLifecycleObserver {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var fetchTasksUseCase: FetchTasksUseCase

    @Inject
    lateinit var rescheduleAllAlarmsUseCase: RescheduleAllAlarmsUseCase

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        fetchTasksUseCase()
        applicationScope.launch { rescheduleAllAlarmsUseCase() }
    }
}
