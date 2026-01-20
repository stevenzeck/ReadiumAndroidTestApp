package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import org.json.JSONObject
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.epub.EpubLayout
import org.readium.r2.shared.publication.presentation.presentation
import org.readium.r2.shared.publication.services.search.isSearchable
import org.readium.r2.shared.util.Try
import javax.inject.Inject

class ReaderSessionFactory @Inject constructor(
    private val preferencesManager: ReaderPreferencesManager,
    private val audioNavigatorFactory: AppAudioNavigatorFactory,
    private val pdfiumDocumentFactory: PdfiumDocumentFactory,
) {

    suspend fun createVisualSession(
        book: Book,
        publication: Publication,
    ): ReaderUiState.Visual {
        val initialLocator = book.progression?.let { Locator.fromJSON(json = JSONObject(it)) }

        val isFixedLayout = publication.metadata.presentation.layout == EpubLayout.FIXED

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

        val navigatorTry = audioNavigatorFactory.createNavigator(
            publication = publication,
            initialLocator = initialLocator,
            initialPreferences = preferences,
        )

        return when (navigatorTry) {
            is Try.Success -> {
                val navigator = navigatorTry.value
                val preferencesEditor = audioNavigatorFactory.createPreferencesEditor(
                    publication = publication,
                    initialPreferences = preferences,
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
                Result.failure(exception = navigatorTry.value)
            }
        }
    }
}
