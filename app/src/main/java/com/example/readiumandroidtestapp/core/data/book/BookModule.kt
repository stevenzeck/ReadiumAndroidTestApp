package com.example.readiumandroidtestapp.core.data.book

import com.example.readiumandroidtestapp.core.data.network.DefaultHttpGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.readium.r2.shared.util.http.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookModule {

    @Provides
    @Singleton
    fun provideHttpGateway(
        httpClient: HttpClient,
    ): HttpGateway = DefaultHttpGateway(httpClient = httpClient)

}
