package com.utakatalp.donebot.di

import com.utakatalp.donebot.data.repository.TaskRepositoryImpl
import com.utakatalp.donebot.data.source.local.datasource.TaskLocalDataSource
import com.utakatalp.donebot.data.source.local.datasource.TaskLocalDataSourceImpl
import com.utakatalp.donebot.domain.repository.TaskRepository
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
}
