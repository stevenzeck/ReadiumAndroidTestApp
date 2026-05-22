package com.example.readiumandroidtestapp.features.reader.ui.state

import com.example.readiumandroidtestapp.core.domain.model.Book
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.navigator.common.NavigationController
import org.readium.navigator.common.RenditionState
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesEditor
import org.readium.navigator.web.fixedlayout.preferences.FixedWebPreferences
import org.readium.navigator.web.fixedlayout.preferences.FixedWebPreferencesEditor
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferences
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferencesEditor
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.preferences.Preference
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

sealed interface ReaderNavigator {
    data class Legacy(val navigator: VisualNavigator) : ReaderNavigator
    data class New(val controller: NavigationController<*, *>) : ReaderNavigator
}

sealed interface ReaderPreferences {
    data class ReflowableWeb(val value: ReflowableWebPreferences) : ReaderPreferences
    data class FixedWeb(val value: FixedWebPreferences) : ReaderPreferences
    data class Pdf(val value: PdfiumPreferences) : ReaderPreferences
    data class Audio(val value: ExoPlayerPreferences) : ReaderPreferences
    data class Tts(val value: AndroidTtsPreferences) : ReaderPreferences
}

sealed interface ReaderPreferencesEditor {
    data class ReflowableWeb(val editor: ReflowableWebPreferencesEditor) : ReaderPreferencesEditor
    data class FixedWeb(val editor: FixedWebPreferencesEditor) : ReaderPreferencesEditor
    data class Pdf(val editor: PdfiumPreferencesEditor) : ReaderPreferencesEditor
    data class Audio(val editor: org.readium.r2.navigator.preferences.PreferencesEditor<ExoPlayerPreferences>) :
        ReaderPreferencesEditor
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
        //FIXME rename to currentLocator
        val initialLocator: Locator?,
        val pdfiumDocumentFactory: PdfiumDocumentFactory,
        val capabilities: ReaderCapabilities,
        val preferencesEditor: ReaderPreferencesEditor? = null,
        val initialPreferences: ReaderPreferences,
        val isFixedLayout: Boolean = false,
        val renditionState: RenditionState<*>? = null,
    ) : ReaderUiState

    data class Audio(
        val publication: Publication,
        val book: Book,
        val navigator: AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>,
        val preferencesEditor: ReaderPreferencesEditor? = null,
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

sealed interface ReaderSettings {
    data class Tts(val session: TtsSettingsSession) : ReaderSettings
    data class Configurable(val editor: ReaderPreferencesEditor) : ReaderSettings
}
