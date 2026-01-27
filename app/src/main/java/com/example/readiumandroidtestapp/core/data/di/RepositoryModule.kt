package com.example.readiumandroidtestapp.core.data.di

import com.example.readiumandroidtestapp.core.data.book.BookRepository
import com.example.readiumandroidtestapp.core.data.book.DefaultBookRepository
import com.example.readiumandroidtestapp.features.reader.data.BookPreferencesRepository
import com.example.readiumandroidtestapp.features.reader.data.DefaultBookPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBookRepository(
        bookRepository: DefaultBookRepository,
    ): BookRepository

    @Binds
    @Singleton
    abstract fun bindBookPreferencesRepository(
        bookPreferencesRepository: DefaultBookPreferencesRepository,
    ): BookPreferencesRepository
}
