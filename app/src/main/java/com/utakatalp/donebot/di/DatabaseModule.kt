package com.utakatalp.donebot.di

import android.content.Context
import androidx.room.Room
import com.utakatalp.donebot.BuildConfig
import com.utakatalp.donebot.data.source.local.AppDatabase
import com.utakatalp.donebot.data.source.local.TaskDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, BuildConfig.DATABASE_NAME).build()

    @Provides
    @Singleton
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()
}
