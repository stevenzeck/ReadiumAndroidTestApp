package com.example.readiumandroidtestapp.core.data.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.http.DefaultHttpClient
import org.readium.r2.shared.util.http.HttpClient
import org.readium.r2.streamer.PublicationOpener
import org.readium.r2.streamer.parser.DefaultPublicationParser
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReadiumModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient = DefaultHttpClient()

    /**
     * Provides a singleton instance of [AssetRetriever].
     *
     * This object is expensive to initialize and is designed to be shared across the application.
     * It handles the retrieval of assets from various sources (local storage, HTTP, etc.).
     */
    @Provides
    @Singleton
    fun provideAssetRetriever(
        @ApplicationContext context: Context,
        httpClient: HttpClient,
    ): AssetRetriever {
        return AssetRetriever(context.contentResolver, httpClient)
    }

    @Provides
    @Singleton
    fun providePdfiumDocumentFactory(context: Context): PdfiumDocumentFactory =
        PdfiumDocumentFactory(context)

    @Provides
    @Singleton
    fun providePublicationParser(
        @ApplicationContext context: Context,
        httpClient: HttpClient,
        assetRetriever: AssetRetriever,
        pdfFactory: PdfiumDocumentFactory,
    ): DefaultPublicationParser = DefaultPublicationParser(
        context = context,
        httpClient = httpClient,
        assetRetriever = assetRetriever,
        pdfFactory = pdfFactory,
        additionalParsers = emptyList(),
    )

    /**
     * Provides a singleton instance of [PublicationOpener].
     *
     * This object is expensive to initialize and holds shared resources like the [AssetRetriever]
     * and parsers. It is responsible for parsing and opening publications (EPUB, PDF, etc.).
     */
    @Provides
    @Singleton
    fun providePublicationOpener(
        @ApplicationContext context: Context,
        httpClient: HttpClient,
        assetRetriever: AssetRetriever,
    ): PublicationOpener {
        val pdfFactory = PdfiumDocumentFactory(context = context)
        val publicationParser = DefaultPublicationParser(
            context = context,
            httpClient = httpClient,
            assetRetriever = assetRetriever,
            pdfFactory = pdfFactory,
        )
        return PublicationOpener(publicationParser, contentProtections = emptyList())
    }
}
