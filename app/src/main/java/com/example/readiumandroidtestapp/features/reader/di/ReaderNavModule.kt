package com.example.readiumandroidtestapp.features.reader.di

import com.example.readiumandroidtestapp.core.navigation.LocalNavigator
import com.example.readiumandroidtestapp.core.navigation.route.NavEntryBuilder
import com.example.readiumandroidtestapp.core.navigation.route.Reader
import com.example.readiumandroidtestapp.features.reader.ui.screens.ReaderScreen
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.multibindings.IntoSet

/**
 * Hilt module that plugs the Reader feature into the application's navigation graph.
 */
@Module
@InstallIn(ActivityRetainedComponent::class)
object ReaderNavModule {

    /**
     * Contributes the [Reader] navigation entry to the global set.
     *
     * This module handles the parameterized route for [Reader], extracting the `bookId`
     * and passing it to the [ReaderScreen].
     */
    @Provides
    @IntoSet
    fun provideReaderEntry(): NavEntryBuilder = {
        entry<Reader> { reader ->
            val navigator = LocalNavigator.current
            ReaderScreen(
                bookId = reader.bookId,
                onNavigateBack = { navigator.goBack() },
            )
        }
    }
}
