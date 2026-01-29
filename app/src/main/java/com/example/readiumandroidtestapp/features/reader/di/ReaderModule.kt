package com.example.readiumandroidtestapp.features.reader.di

import com.example.readiumandroidtestapp.features.reader.data.DefaultSearchGateway
import com.example.readiumandroidtestapp.features.reader.data.DefaultTtsServiceGateway
import com.example.readiumandroidtestapp.features.reader.domain.SearchGateway
import com.example.readiumandroidtestapp.features.reader.domain.TtsServiceGateway
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.DefaultAppAudioNavigatorFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReaderModule {

    @Binds
    @Singleton
    abstract fun bindAppAudioNavigatorFactory(
        factory: DefaultAppAudioNavigatorFactory,
    ): AppAudioNavigatorFactory

    @Binds
    abstract fun bindSearchGateway(
        gateway: DefaultSearchGateway,
    ): SearchGateway

    @Binds
    abstract fun bindTtsServiceGateway(
        gateway: DefaultTtsServiceGateway,
    ): TtsServiceGateway
}
