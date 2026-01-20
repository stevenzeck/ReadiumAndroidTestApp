package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import com.example.readiumandroidtestapp.features.reader.data.BookPreferencesRepository
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.TtsSettingsSession
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumDefaults
import org.readium.adapter.pdfium.navigator.PdfiumEngineProvider
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesSerializer
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesSerializer
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.epub.EpubDefaults
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.epub.EpubPreferencesSerializer
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.navigator.preferences.map
import org.readium.r2.shared.publication.Publication
import javax.inject.Inject

class ReaderPreferencesManager @Inject constructor(
    private val bookPreferencesRepository: BookPreferencesRepository,
    private val audioNavigatorFactory: AppAudioNavigatorFactory,
) {

    /**
     * Commits the given preferences to the active navigator and saves them to the repository.
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun commitPreferences(
        bookId: Long,
        preferences: Configurable.Preferences<*>,
        currentVisualNavigator: VisualNavigator?,
        audioNavigator: AudioNavigator<*, *>?,
        ttsManager: ReaderTtsManager,
    ) {
        when (preferences) {
            is EpubPreferences -> {
                val nav = currentVisualNavigator as? Configurable<*, EpubPreferences>
                nav?.submitPreferences(preferences)
            }

            is PdfiumPreferences -> {
                val nav = currentVisualNavigator as? Configurable<*, PdfiumPreferences>
                nav?.submitPreferences(preferences)
            }

            is ExoPlayerPreferences -> {
                val nav = audioNavigator as? Configurable<*, ExoPlayerPreferences>
                nav?.submitPreferences(preferences)
            }

            is AndroidTtsPreferences -> {
                ttsManager.submitPreferences(preferences)
            }
        }

        val json = when (preferences) {
            is EpubPreferences -> EpubPreferencesSerializer().serialize(preferences)
            is PdfiumPreferences -> PdfiumPreferencesSerializer().serialize(preferences)
            is AndroidTtsPreferences -> AndroidTtsPreferencesSerializer().serialize(preferences)
            is ExoPlayerPreferences -> ExoPlayerPreferencesSerializer().serialize(preferences)
            else -> null
        }

        if (json != null) {
            when (preferences) {
                is AndroidTtsPreferences -> bookPreferencesRepository.saveTtsPreferences(
                    bookId,
                    preferencesJson = json,
                )

                is ExoPlayerPreferences -> bookPreferencesRepository.saveAudiobookPreferences(
                    bookId,
                    preferencesJson = json,
                )

                else -> bookPreferencesRepository.savePreferences(bookId, preferencesJson = json)
            }
        }
    }

    /**
     * Creates a new PreferencesEditor based on the changed preferences.
     * This is required because Editors are immutable and bound to specific preference instances.
     */
    fun createPreferencesEditor(
        publication: Publication,
        preferences: Configurable.Preferences<*>,
    ): PreferencesEditor<*>? {
        return when (preferences) {
            is EpubPreferences -> {
                val factory = EpubNavigatorFactory(
                    publication = publication,
                    configuration = EpubNavigatorFactory.Configuration(defaults = EpubDefaults()),
                )
                factory.createPreferencesEditor(currentPreferences = preferences)
            }

            is PdfiumPreferences -> {
                val factory = PdfNavigatorFactory(
                    publication = publication,
                    pdfEngineProvider = PdfiumEngineProvider(
                        defaults = PdfiumDefaults(),
                    ),
                )
                factory.createPreferencesEditor(initialPreferences = preferences)
            }

            is ExoPlayerPreferences -> {
                audioNavigatorFactory.createPreferencesEditor(
                    publication = publication,
                    initialPreferences = preferences,
                )
            }

            else -> null
        }
    }

    /**
     * Loads and deserializes preferences for the given book and profile.
     */
    suspend fun loadPreferences(
        bookId: Long,
        publication: Publication,
    ): Configurable.Preferences<*> {
        val json = bookPreferencesRepository.getPreferences(bookId)

        return if (publication.conformsTo(profile = Publication.Profile.EPUB)) {
            json?.let { EpubPreferencesSerializer().deserialize(preferences = it) }
                ?: EpubPreferences()
        } else if (publication.conformsTo(profile = Publication.Profile.PDF)) {
            json?.let { PdfiumPreferencesSerializer().deserialize(preferences = it) }
                ?: PdfiumPreferences()
        } else {
            EpubPreferences()
        }
    }

    suspend fun loadAudiobookPreferences(bookId: Long): ExoPlayerPreferences {
        val json = bookPreferencesRepository.getAudiobookPreferences(bookId)
        return json?.let { ExoPlayerPreferencesSerializer().deserialize(preferences = it) }
            ?: ExoPlayerPreferences()
    }

    /**
     * Builds the TTS Settings Session (Voices, Languages, Editor).
     */
    suspend fun createTtsSettingsSession(
        bookId: Long,
        publication: Publication,
        ttsManager: ReaderTtsManager,
        application: Application,
    ): TtsSettingsSession? {
        val json = bookPreferencesRepository.getTtsPreferences(bookId)
        val preferences =
            json?.let { AndroidTtsPreferencesSerializer().deserialize(preferences = it) }
                ?: AndroidTtsPreferences()

        val factory = org.readium.navigator.media.tts.AndroidTtsNavigatorFactory(
            application,
            publication,
        ) ?: return null
        val editor = factory.createPreferencesEditor(preferences)

        val voices = ttsManager.voices
        val allLanguages =
            listOf(null) + voices.map { it.language }.distinct().sortedBy { it.locale.displayName }

        val currentLanguage = editor.language.value ?: publication.metadata.language
        val supportedVoices = if (currentLanguage != null) {
            voices.filter {
                it.language.removeRegion() == currentLanguage.removeRegion()
            }
        } else {
            voices
        }.sortedBy { it.id.toString() }

        val voicePreference = editor.voices.map(
            from = { voicesMap ->
                val voiceId = currentLanguage?.let { voicesMap[it] }
                supportedVoices.find { it.id == voiceId } ?: supportedVoices.firstOrNull()
            },
            to = { voice ->
                val map = editor.voices.value ?: emptyMap()
                if (currentLanguage != null && voice != null) {
                    map + (currentLanguage to voice.id)
                } else {
                    map
                }
            },
        )

        return TtsSettingsSession(
            editor = editor,
            voice = voicePreference,
            availableLanguages = allLanguages,
            availableVoices = supportedVoices,
        )
    }

    /**
     * Creates a new ReaderUiState with a refreshed PreferencesEditor based on the new preferences.
     * Handles the specific casting required for Audio vs Visual states.
     */
    fun refreshSessionState(
        currentState: ReaderUiState,
        newPreferences: Configurable.Preferences<*>,
    ): ReaderUiState? {
        return when (currentState) {
            is ReaderUiState.Visual -> {
                val newEditor = createPreferencesEditor(
                    publication = currentState.publication,
                    preferences = newPreferences,
                ) ?: return null

                currentState.copy(preferencesEditor = newEditor)
            }

            is ReaderUiState.Audio -> {
                val newEditor = createPreferencesEditor(
                    publication = currentState.publication,
                    preferences = newPreferences,
                ) ?: return null

                @Suppress("UNCHECKED_CAST") val audioEditor =
                    newEditor as? PreferencesEditor<ExoPlayerPreferences>
                currentState.copy(preferencesEditor = audioEditor)
            }

            else -> null
        }
    }
}
