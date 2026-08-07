package com.example.readiumandroidtestapp.features.reader.di

import com.example.readiumandroidtestapp.features.reader.data.DefaultPreferencesSerializerFactory
import com.example.readiumandroidtestapp.features.reader.data.DefaultSearchGateway
import com.example.readiumandroidtestapp.features.reader.data.PreferencesSerializerFactory
import com.example.readiumandroidtestapp.features.reader.domain.SearchGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReaderModule {

    @Binds
    abstract fun bindSearchGateway(
        gateway: DefaultSearchGateway,
    ): SearchGateway

    @Binds
    abstract fun bindPreferencesSerializerFactory(
        factory: DefaultPreferencesSerializerFactory,
    ): PreferencesSerializerFactory
}
