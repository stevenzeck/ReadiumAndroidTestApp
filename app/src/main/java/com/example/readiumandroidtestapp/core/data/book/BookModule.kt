package com.example.readiumandroidtestapp.core.data.book

import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.data.di.IoDispatcher
import com.example.readiumandroidtestapp.core.data.network.DefaultHttpGateway
import com.example.readiumandroidtestapp.core.domain.gateway.AssetRetrieverGateway
import com.example.readiumandroidtestapp.core.domain.gateway.PublicationOpenerGateway
import com.example.readiumandroidtestapp.core.domain.network.HttpGateway
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import org.readium.r2.shared.util.http.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookModule {

    @Provides
    @Singleton
    fun provideCoverImageSaver(
        storageGateway: StorageGateway,
    ): CoverImageSaver = DefaultCoverImageSaver(storageGateway = storageGateway)

    @Provides
    @Singleton
    fun provideHttpGateway(
        httpClient: HttpClient,
    ): HttpGateway = DefaultHttpGateway(httpClient = httpClient)

    @Provides
    @Singleton
    fun provideBookImporter(
        storageGateway: StorageGateway,
        booksDao: BooksDao,
        assetRetriever: AssetRetrieverGateway,
        publicationOpener: PublicationOpenerGateway,
        httpGateway: HttpGateway,
        coverImageSaver: CoverImageSaver,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
    ): BookImporter =
        DefaultBookImporter(
            storageGateway = storageGateway,
            booksDao = booksDao,
            assetRetriever = assetRetriever,
            publicationOpener = publicationOpener,
            httpGateway = httpGateway,
            coverImageSaver = coverImageSaver,
            ioDispatcher = ioDispatcher,
        )

}
