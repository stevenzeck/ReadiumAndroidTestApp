package com.example.readiumandroidtestapp.core.data.di

import com.example.readiumandroidtestapp.core.data.opds.DefaultOpdsParser
import com.example.readiumandroidtestapp.core.domain.opds.OpdsParser
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class OpdsModule {

    @Binds
    @Singleton
    abstract fun bindOpdsParser(
        opdsParser: DefaultOpdsParser,
    ): OpdsParser
}
