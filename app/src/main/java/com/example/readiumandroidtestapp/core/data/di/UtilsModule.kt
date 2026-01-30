package com.example.readiumandroidtestapp.core.data.di

import com.example.readiumandroidtestapp.core.utils.DefaultUserMessageManager
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilsModule {

    @Binds
    @Singleton
    abstract fun bindUserMessageManager(
        manager: DefaultUserMessageManager,
    ): UserMessageManager
}
