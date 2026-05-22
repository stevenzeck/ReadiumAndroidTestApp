package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferences
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferencesEditor
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import kotlinx.collections.immutable.persistentListOf
import org.json.JSONObject
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.navigator.web.fixedlayout.FixedWebConfiguration
import org.readium.navigator.web.fixedlayout.FixedWebGoLocation
import org.readium.navigator.web.fixedlayout.FixedWebRenditionFactory
import org.readium.navigator.web.fixedlayout.preferences.FixedWebPreferences
import org.readium.navigator.web.reflowable.ReflowableWebConfiguration
import org.readium.navigator.web.reflowable.ReflowableWebGoLocation
import org.readium.navigator.web.reflowable.ReflowableWebRenditionFactory
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferences
import org.readium.r2.shared.publication.Layout
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.search.isSearchable
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.getOrElse
import javax.inject.Inject

class DefaultReaderSessionFactory @Inject constructor(
    private val application: Application,
    private val preferencesManager: ReaderPreferencesManager,
    private val audioNavigatorFactory: AppAudioNavigatorFactory,
    private val pdfiumDocumentFactory: PdfiumDocumentFactory,
) : ReaderSessionFactory {

    override suspend fun createVisualSession(
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

        val renditionState = if (publication.conformsTo(profile = Publication.Profile.EPUB)) {
            if (isFixedLayout) {
                val config = FixedWebConfiguration()
                val factory = FixedWebRenditionFactory(
                    application = application,
                    publication = publication,
                    configuration = config,
                )
                val initialLocation = initialLocator?.let { FixedWebGoLocation(href = it.href) }
                val initialWebPreferences =
                    (initialPreferences as? ReaderPreferences.FixedWeb)?.value
                        ?: FixedWebPreferences()
                val initialSettings =
                    factory?.createPreferencesEditor(initialPreferences = initialWebPreferences)?.settings

                if (initialSettings != null) {
                    factory.createRenditionState(
                        initialSettings = initialSettings,
                        initialLocation = initialLocation,
                    ).getOrElse { null }
                } else {
                    null
                }
            } else {
                val config = ReflowableWebConfiguration(
                    servedAssets = persistentListOf("fonts/.*"),
                )
                val factory = ReflowableWebRenditionFactory(
                    application = application,
                    publication = publication,
                    configuration = config,
                )
                val initialLocation =
                    initialLocator?.let { ReflowableWebGoLocation(href = it.href) }
                val initialWebPreferences =
                    (initialPreferences as? ReaderPreferences.ReflowableWeb)?.value
                        ?: ReflowableWebPreferences()
                val initialSettings =
                    factory?.createPreferencesEditor(initialPreferences = initialWebPreferences)?.settings

                if (initialSettings != null) {
                    factory.createRenditionState(
                        initialSettings = initialSettings,
                        initialLocation = initialLocation,
                    ).getOrElse { null }
                } else {
                    null
                }
            }
        } else {
            null
        }

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
            renditionState = renditionState,
        )
    }

    override suspend fun createAudioSession(
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
                val rawEditor = audioNavigatorFactory.createPreferencesEditor(
                    publication = publication,
                    initialPreferences = preferences,
                )
                val preferencesEditor = rawEditor?.let {
                    ReaderPreferencesEditor.Audio(
                        editor = it,
                    )
                }

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
