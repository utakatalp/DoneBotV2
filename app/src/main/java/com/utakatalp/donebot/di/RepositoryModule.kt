package com.utakatalp.donebot.di

import com.utakatalp.donebot.data.engine.PomodoroEngineImpl
import com.utakatalp.donebot.data.repository.AuthRepositoryImpl
import com.utakatalp.donebot.data.repository.AuthSessionRepositoryImpl
import com.utakatalp.donebot.data.repository.PomodoroSettingsRepositoryImpl
import com.utakatalp.donebot.data.repository.ReminderSettingsRepositoryImpl
import com.utakatalp.donebot.data.repository.TaskRepositoryImpl
import com.utakatalp.donebot.data.repository.UserRepositoryImpl
import com.utakatalp.donebot.data.source.local.datasource.TaskLocalDataSource
import com.utakatalp.donebot.data.source.local.datasource.TaskLocalDataSourceImpl
import com.utakatalp.donebot.data.source.remote.datasource.TaskRemoteDataSource
import com.utakatalp.donebot.data.source.remote.datasource.TaskRemoteDataSourceImpl
import com.utakatalp.donebot.data.sync.FetchTasksUseCaseImpl
import com.utakatalp.donebot.data.sync.SyncPendingTasksUseCaseImpl
import com.utakatalp.donebot.domain.engine.PomodoroEngine
import com.utakatalp.donebot.domain.repository.AuthRepository
import com.utakatalp.donebot.domain.repository.AuthSessionRepository
import com.utakatalp.donebot.domain.repository.PomodoroSettingsRepository
import com.utakatalp.donebot.domain.repository.ReminderSettingsRepository
import com.utakatalp.donebot.domain.repository.TaskRepository
import com.utakatalp.donebot.domain.repository.UserRepository
import com.utakatalp.donebot.domain.usecase.FetchTasksUseCase
import com.utakatalp.donebot.domain.usecase.SyncPendingTasksUseCase
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindTaskLocalDataSource(impl: TaskLocalDataSourceImpl): TaskLocalDataSource

    @Binds @Singleton
    abstract fun bindTaskRemoteDataSource(impl: TaskRemoteDataSourceImpl): TaskRemoteDataSource

    @Binds @Singleton
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds @Singleton
    abstract fun bindAuthSessionRepository(impl: AuthSessionRepositoryImpl): AuthSessionRepository

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds @Singleton
    abstract fun bindPomodoroSettingsRepository(impl: PomodoroSettingsRepositoryImpl): PomodoroSettingsRepository

    @Binds @Singleton
    abstract fun bindReminderSettingsRepository(impl: ReminderSettingsRepositoryImpl): ReminderSettingsRepository

    @Binds @Singleton
    abstract fun bindPomodoroEngine(impl: PomodoroEngineImpl): PomodoroEngine

    @Binds
    abstract fun bindSyncPendingTasksUseCase(impl: SyncPendingTasksUseCaseImpl): SyncPendingTasksUseCase

    @Binds
    abstract fun bindFetchTasksUseCase(impl: FetchTasksUseCaseImpl): FetchTasksUseCase
}
