package com.example.readiumandroidtestapp.features.reader.ui.state

import com.example.readiumandroidtestapp.core.domain.model.Book
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesEditor
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Language

/**
 * Represents specific error states for the Reader.
 */
sealed class ReaderError {
    data object InvalidBookLocation : ReaderError()
    data class AssetRetrievalFailed(val cause: Throwable) : ReaderError()
    data class PublicationOpenFailed(val cause: Throwable) : ReaderError()
    data class NavigatorCreationFailed(val cause: Throwable) : ReaderError()
}

data class ReaderCapabilities(
    val isSearchable: Boolean,
    val canSpeak: Boolean,
    val hasPreferences: Boolean,
)

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Error(val error: ReaderError) : ReaderUiState
    data class Visual(
        val publication: Publication,
        val book: Book,
        val initialLocator: Locator?,
        val pdfiumDocumentFactory: PdfiumDocumentFactory,
        val capabilities: ReaderCapabilities,
        val preferencesEditor: PreferencesEditor<*>? = null,
        val initialPreferences: Configurable.Preferences<*>,
        val isFixedLayout: Boolean = false,
    ) : ReaderUiState

    data class Audio(
        val publication: Publication,
        val book: Book,
        val navigator: AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>,
        val preferencesEditor: PreferencesEditor<ExoPlayerPreferences>? = null,
    ) : ReaderUiState
}

sealed class SearchItem {
    data class Header(val title: String) : SearchItem()
    data class Result(val locator: Locator) : SearchItem()
}

data class TtsSettingsSession(
    val editor: AndroidTtsPreferencesEditor,
    val voice: Preference<AndroidTtsEngine.Voice?>,
    val availableLanguages: List<Language?>,
    val availableVoices: List<AndroidTtsEngine.Voice>,
)

sealed interface ReaderSettingsSheet {
    data class Tts(val session: TtsSettingsSession) : ReaderSettingsSheet
    data class Configurable(val editor: PreferencesEditor<*>) : ReaderSettingsSheet
}
