package com.example.readiumandroidtestapp.core.data.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private val Context.bookPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
        name = "book_preferences",
    )

    @Provides
    @Singleton
    fun provideBookPreferencesDataStore(
        @ApplicationContext context: Context,
    ): DataStore<Preferences> {
        return context.bookPreferencesDataStore
    }
}
