package com.example.readiumandroidtestapp.core.data.di

import com.example.readiumandroidtestapp.core.data.storage.AndroidStorageGateway
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Binds
    @Singleton
    abstract fun bindStorageGateway(
        androidStorageGateway: AndroidStorageGateway,
    ): StorageGateway
}
