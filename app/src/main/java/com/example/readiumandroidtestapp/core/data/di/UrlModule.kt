package com.example.readiumandroidtestapp.core.data.di

import com.example.readiumandroidtestapp.core.data.gateway.DefaultUrlGateway
import com.example.readiumandroidtestapp.core.domain.gateway.UrlGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UrlModule {

    @Binds
    @Singleton
    abstract fun bindUrlGateway(
        defaultUrlGateway: DefaultUrlGateway,
    ): UrlGateway
}
