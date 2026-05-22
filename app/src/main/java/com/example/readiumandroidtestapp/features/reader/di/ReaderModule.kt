package com.example.readiumandroidtestapp.features.reader.di

import com.example.readiumandroidtestapp.features.reader.data.AndroidTtsNavigatorFactoryProvider
import com.example.readiumandroidtestapp.features.reader.data.AndroidTtsNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.DefaultAndroidTtsNavigatorFactoryProvider
import com.example.readiumandroidtestapp.features.reader.data.DefaultAndroidTtsNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.DefaultPdfNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.DefaultPreferencesSerializerFactory
import com.example.readiumandroidtestapp.features.reader.data.DefaultSearchGateway
import com.example.readiumandroidtestapp.features.reader.data.DefaultTtsServiceGateway
import com.example.readiumandroidtestapp.features.reader.data.PdfNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.PreferencesSerializerFactory
import com.example.readiumandroidtestapp.features.reader.domain.DefaultOpenPublicationUseCase
import com.example.readiumandroidtestapp.features.reader.domain.DefaultReaderDecorationManager
import com.example.readiumandroidtestapp.features.reader.domain.DefaultReaderPreferencesManager
import com.example.readiumandroidtestapp.features.reader.domain.DefaultReaderSessionFactory
import com.example.readiumandroidtestapp.features.reader.domain.OpenPublicationUseCase
import com.example.readiumandroidtestapp.features.reader.domain.ReaderDecorationManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderPreferencesManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderSessionFactory
import com.example.readiumandroidtestapp.features.reader.domain.SearchGateway
import com.example.readiumandroidtestapp.features.reader.domain.TtsServiceGateway
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.AudioNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.ui.audio.DefaultAppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.DefaultAudioNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.ui.audio.DefaultMediaSessionFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.DefaultReaderMediaBinder
import com.example.readiumandroidtestapp.features.reader.ui.audio.MediaSessionFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.ReaderMediaBinder
import com.example.readiumandroidtestapp.features.reader.ui.search.DefaultReaderSearchManager
import com.example.readiumandroidtestapp.features.reader.ui.search.ReaderSearchManager
import com.example.readiumandroidtestapp.features.reader.ui.tts.DefaultReaderTtsManager
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
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
    @Singleton
    abstract fun bindAudioNavigatorFactoryWrapper(
        wrapper: DefaultAudioNavigatorFactoryWrapper,
    ): AudioNavigatorFactoryWrapper

    @Binds
    abstract fun bindAndroidTtsNavigatorFactoryWrapper(
        wrapper: DefaultAndroidTtsNavigatorFactoryWrapper,
    ): AndroidTtsNavigatorFactoryWrapper

    @Binds
    abstract fun bindAndroidTtsNavigatorFactoryProvider(
        provider: DefaultAndroidTtsNavigatorFactoryProvider,
    ): AndroidTtsNavigatorFactoryProvider

    @Binds
    abstract fun bindSearchGateway(
        gateway: DefaultSearchGateway,
    ): SearchGateway

    @Binds
    abstract fun bindTtsServiceGateway(
        gateway: DefaultTtsServiceGateway,
    ): TtsServiceGateway

    @Binds
    abstract fun bindReaderSearchManager(
        manager: DefaultReaderSearchManager,
    ): ReaderSearchManager

    @Binds
    abstract fun bindReaderTtsManager(
        manager: DefaultReaderTtsManager,
    ): ReaderTtsManager

    @Binds
    abstract fun bindReaderDecorationManager(
        manager: DefaultReaderDecorationManager,
    ): ReaderDecorationManager

    @Binds
    abstract fun bindReaderPreferencesManager(
        manager: DefaultReaderPreferencesManager,
    ): ReaderPreferencesManager

    @Binds
    abstract fun bindReaderSessionFactory(
        factory: DefaultReaderSessionFactory,
    ): ReaderSessionFactory

    @Binds
    abstract fun bindReaderMediaBinder(
        binder: DefaultReaderMediaBinder,
    ): ReaderMediaBinder

    @Binds
    abstract fun bindOpenPublicationUseCase(
        useCase: DefaultOpenPublicationUseCase,
    ): OpenPublicationUseCase

    @Binds
    abstract fun bindMediaSessionFactory(
        factory: DefaultMediaSessionFactory,
    ): MediaSessionFactory

    @Binds
    abstract fun bindPdfNavigatorFactoryWrapper(
        wrapper: DefaultPdfNavigatorFactoryWrapper,
    ): PdfNavigatorFactoryWrapper

    @Binds
    abstract fun bindPreferencesSerializerFactory(
        factory: DefaultPreferencesSerializerFactory,
    ): PreferencesSerializerFactory
}
