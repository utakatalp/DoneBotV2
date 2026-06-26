package com.utakatalp.donebot.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // Retrofit and OkHttp providers go here when networking is added
}
