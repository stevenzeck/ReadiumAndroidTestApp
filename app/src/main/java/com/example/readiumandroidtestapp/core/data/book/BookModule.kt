package com.example.readiumandroidtestapp.core.data.book

import com.example.readiumandroidtestapp.core.data.database.BooksDao
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.HttpClient
import org.readium.r2.streamer.PublicationOpener
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BookModule {

    @Provides
    @Singleton
    fun provideBookImporter(
        storageGateway: StorageGateway,
        booksDao: BooksDao,
        assetRetriever: AssetRetriever,
        publicationOpener: PublicationOpener,
        httpClient: HttpClient,
    ): BookImporter =
        BookImporter(
            storageGateway = storageGateway,
            booksDao = booksDao,
            assetRetriever = assetRetriever,
            publicationOpener = publicationOpener,
            httpClient = httpClient,
        )

    @Provides
    @Singleton
    fun provideBookRepository(
        booksDao: BooksDao,
        bookImporter: BookImporter,
    ): BookRepository =
        BookRepository(
            booksDao = booksDao,
            bookImporter = bookImporter,
        )
}
