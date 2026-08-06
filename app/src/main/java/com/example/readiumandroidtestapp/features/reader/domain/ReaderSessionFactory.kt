package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import android.content.Context
import androidx.media3.common.MediaMetadata
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import org.readium.adapter.exoplayer.audio.ExoPlayerEngineProvider
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.navigator.media.audio.AudioNavigatorFactory
import org.readium.navigator.media.common.MediaMetadataFactory
import org.readium.navigator.media.common.MediaMetadataProvider
import org.readium.r2.shared.publication.Layout
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.search.isSearchable
import org.readium.r2.shared.util.Try
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReaderSessionFactory @Inject constructor(
    @ApplicationContext private val applicationContext: Context,
    private val preferencesManager: ReaderPreferencesManager,
    private val pdfiumDocumentFactory: PdfiumDocumentFactory,
) {

    suspend fun createVisualSession(
        book: Book,
        publication: Publication,
    ): ReaderUiState.Visual {
        val initialLocator = book.progression?.let { Locator.fromJSON(json = JSONObject(it)) }

        val isFixedLayout = publication.metadata.layout == Layout.FIXED

        val initialPreferences = preferencesManager.loadPreferences(
            bookId = book.id,
            publication = publication,
        )
        val preferencesEditor = preferencesManager.createPreferencesEditor(
            publication = publication,
            preferences = initialPreferences,
        )

        // TTS enabled if EPUB (reflowable) or PDF
        val hasTts =
            (publication.conformsTo(profile = Publication.Profile.EPUB) && !isFixedLayout) || publication.conformsTo(
                profile = Publication.Profile.PDF,
            )

        val capabilities = ReaderCapabilities(
            isSearchable = publication.isSearchable,
            canSpeak = hasTts,
            hasPreferences = preferencesEditor != null,
        )

        return ReaderUiState.Visual(
            publication = publication,
            book = book,
            initialLocator = initialLocator,
            pdfiumDocumentFactory = pdfiumDocumentFactory,
            capabilities = capabilities,
            preferencesEditor = preferencesEditor,
            initialPreferences = initialPreferences,
            isFixedLayout = isFixedLayout,
        )
    }

    suspend fun createAudioSession(
        book: Book,
        publication: Publication,
    ): Result<ReaderUiState.Audio> {
        val initialLocator = book.progression?.let { Locator.fromJSON(json = JSONObject(it)) }
        val preferences = preferencesManager.loadAudiobookPreferences(bookId = book.id)

        val metadataProvider = MediaMetadataProvider { pub ->
            ChapterMediaMetadataFactory(
                bookTitle = book.title
                    ?: applicationContext.getString(R.string.unknown_title),
                bookAuthor = book.author
                    ?: applicationContext.getString(R.string.unknown_author),
                publication = pub,
                unknownAuthorString = applicationContext.getString(R.string.unknown_author),
            )
        }

        val factory = AudioNavigatorFactory(
            publication = publication,
            audioEngineProvider = ExoPlayerEngineProvider(
                application = applicationContext as Application,
                metadataProvider = metadataProvider,
            ),
        )

        if (factory == null) {
            return Result.failure(Exception("Failed to create AudioNavigatorFactory: publication might not be supported"))
        }

        val navigatorTry = factory.createNavigator(
            initialLocator = initialLocator,
            initialPreferences = preferences,
            readingOrder = publication.readingOrder,
        )

        return when (navigatorTry) {
            is Try.Success -> {
                val navigator = navigatorTry.value
                val preferencesEditor = factory.createAudioPreferencesEditor(
                    currentPreferences = preferences,
                )

                val state = ReaderUiState.Audio(
                    publication = publication,
                    book = book,
                    navigator = navigator,
                    preferencesEditor = preferencesEditor,
                )
                Result.success(value = state)
            }

            is Try.Failure -> {
                Result.failure(exception = Exception("Failed to create AudioNavigator: ${navigatorTry.value.message}"))
            }
        }
    }
}

class ChapterMediaMetadataFactory(
    private val bookTitle: String,
    private val bookAuthor: String?,
    private val publication: Publication,
    private val unknownAuthorString: String,
) : MediaMetadataFactory {

    override suspend fun publicationMetadata(): MediaMetadata =
        MediaMetadata.Builder()
            .setTitle(bookTitle)
            .setArtist(bookAuthor ?: unknownAuthorString)
            .build()

    override suspend fun resourceMetadata(index: Int): MediaMetadata {
        val chapterTitle = publication.readingOrder.getOrNull(index)?.title
        return MediaMetadata.Builder()
            .setTitle(bookTitle)
            .setArtist(chapterTitle ?: bookAuthor ?: unknownAuthorString)
            .build()
    }
}
