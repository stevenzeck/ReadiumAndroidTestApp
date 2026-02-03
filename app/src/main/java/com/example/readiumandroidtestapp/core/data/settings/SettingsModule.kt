package com.example.readiumandroidtestapp.core.data.settings

import com.example.readiumandroidtestapp.core.data.repository.DataStoreSettingsRepository
import com.example.readiumandroidtestapp.core.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsModule {

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        dataStoreSettingsRepository: DataStoreSettingsRepository,
    ): SettingsRepository
}
