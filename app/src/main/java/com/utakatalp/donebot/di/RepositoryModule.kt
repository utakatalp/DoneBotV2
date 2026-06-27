package com.utakatalp.donebot.di

import com.utakatalp.donebot.data.repository.AuthRepositoryImpl
import com.utakatalp.donebot.data.repository.SessionPreferencesImpl
import com.utakatalp.donebot.data.repository.TaskRepositoryImpl
import com.utakatalp.donebot.data.repository.UserRepositoryImpl
import com.utakatalp.donebot.data.source.local.datasource.TaskLocalDataSource
import com.utakatalp.donebot.data.source.local.datasource.TaskLocalDataSourceImpl
import com.utakatalp.donebot.domain.repository.AuthRepository
import com.utakatalp.donebot.domain.repository.SessionPreferences
import com.utakatalp.donebot.domain.repository.TaskRepository
import com.utakatalp.donebot.domain.repository.UserRepository
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
    abstract fun bindTaskRepository(impl: TaskRepositoryImpl): TaskRepository

    @Binds @Singleton
    abstract fun bindSessionPreferences(impl: SessionPreferencesImpl): SessionPreferences

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
