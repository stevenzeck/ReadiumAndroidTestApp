package com.example.readiumandroidtestapp.core.data.di

import com.example.readiumandroidtestapp.core.data.gateway.DefaultAssetRetrieverGateway
import com.example.readiumandroidtestapp.core.data.gateway.DefaultPublicationOpenerGateway
import com.example.readiumandroidtestapp.core.domain.gateway.AssetRetrieverGateway
import com.example.readiumandroidtestapp.core.domain.gateway.PublicationOpenerGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class GatewayModule {

    @Binds
    @Singleton
    abstract fun bindAssetRetrieverGateway(
        impl: DefaultAssetRetrieverGateway
    ): AssetRetrieverGateway

    @Binds
    @Singleton
    abstract fun bindPublicationOpenerGateway(
        impl: DefaultPublicationOpenerGateway
    ): PublicationOpenerGateway
}
