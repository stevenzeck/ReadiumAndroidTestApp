package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import com.example.readiumandroidtestapp.features.reader.data.AndroidTtsNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.BookPreferencesRepository
import com.example.readiumandroidtestapp.features.reader.data.PdfNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.PreferencesSerializerFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderNavigator
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferences
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferencesEditor
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.TtsSettingsSession
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.navigator.common.SettingsController
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.navigator.web.fixedlayout.FixedWebConfiguration
import org.readium.navigator.web.fixedlayout.FixedWebRenditionFactory
import org.readium.navigator.web.fixedlayout.preferences.FixedWebPreferences
import org.readium.navigator.web.fixedlayout.preferences.FixedWebSettings
import org.readium.navigator.web.reflowable.ReflowableWebConfiguration
import org.readium.navigator.web.reflowable.ReflowableWebRenditionFactory
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferences
import org.readium.navigator.web.reflowable.preferences.ReflowableWebSettings
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.map
import org.readium.r2.shared.publication.Layout
import org.readium.r2.shared.publication.Publication
import javax.inject.Inject

class DefaultReaderPreferencesManager @Inject constructor(
    private val application: Application,
    private val bookPreferencesRepository: BookPreferencesRepository,
    private val audioNavigatorFactory: AppAudioNavigatorFactory,
    private val ttsNavigatorFactoryWrapper: AndroidTtsNavigatorFactoryWrapper,
    private val pdfNavigatorFactoryWrapper: PdfNavigatorFactoryWrapper,
    private val preferencesSerializerFactory: PreferencesSerializerFactory,
) : ReaderPreferencesManager {

    /**
     * Commits the given preferences to the active navigator and saves them to the repository.
     */
    @Suppress("UNCHECKED_CAST")
    override suspend fun commitPreferences(
        bookId: Long,
        preferences: ReaderPreferences,
        publication: Publication,
        navigator: ReaderNavigator?,
        audioNavigator: AudioNavigator<*, *>?,
        ttsManager: ReaderTtsManager,
    ) {
        when (preferences) {
            is ReaderPreferences.ReflowableWeb -> {
                (navigator as? ReaderNavigator.New)?.let { readerNav ->
                    (readerNav.controller as? SettingsController<ReflowableWebSettings>)?.let { controller ->
                        ReflowableWebRenditionFactory(
                            application = application,
                            publication = publication,
                            configuration = ReflowableWebConfiguration(),
                        )?.createPreferencesEditor(initialPreferences = preferences.value)
                            ?.let { editor ->
                                controller.settings = editor.settings
                            }
                    }
                }
            }

            is ReaderPreferences.FixedWeb -> {
                (navigator as? ReaderNavigator.New)?.let { readerNav ->
                    (readerNav.controller as? SettingsController<FixedWebSettings>)?.let { controller ->
                        FixedWebRenditionFactory(
                            application = application,
                            publication = publication,
                            configuration = FixedWebConfiguration(),
                        )?.createPreferencesEditor(initialPreferences = preferences.value)
                            ?.let { editor ->
                                controller.settings = editor.settings
                            }
                    }
                }
            }

            is ReaderPreferences.Pdf -> {
                (navigator as? ReaderNavigator.Legacy)?.let { readerNav ->
                    val nav = readerNav.navigator as? Configurable<*, PdfiumPreferences>
                    nav?.submitPreferences(preferences = preferences.value)
                }
            }

            is ReaderPreferences.Audio -> {
                val nav = audioNavigator as? Configurable<*, ExoPlayerPreferences>
                nav?.submitPreferences(preferences = preferences.value)
            }

            is ReaderPreferences.Tts -> {
                ttsManager.submitPreferences(preferences = preferences.value)
            }
        }

        val json = when (preferences) {
            is ReaderPreferences.FixedWeb -> preferencesSerializerFactory.createFixedWebSerializer()
                .serialize(preferences = preferences.value)

            is ReaderPreferences.ReflowableWeb -> preferencesSerializerFactory.createReflowableWebSerializer()
                .serialize(preferences = preferences.value)

            is ReaderPreferences.Pdf -> preferencesSerializerFactory.createPdfiumSerializer()
                .serialize(preferences = preferences.value)

            is ReaderPreferences.Tts -> preferencesSerializerFactory.createAndroidTtsSerializer()
                .serialize(preferences = preferences.value)

            is ReaderPreferences.Audio -> preferencesSerializerFactory.createExoPlayerSerializer()
                .serialize(preferences = preferences.value)
        }

        when (preferences) {
            is ReaderPreferences.Tts -> bookPreferencesRepository.saveTtsPreferences(
                bookId = bookId,
                preferencesJson = json,
            )

            is ReaderPreferences.Audio -> bookPreferencesRepository.saveAudiobookPreferences(
                bookId = bookId,
                preferencesJson = json,
            )

            else -> bookPreferencesRepository.savePreferences(
                bookId = bookId,
                preferencesJson = json,
            )
        }
    }

    /**
     * Creates a new PreferencesEditor based on the changed preferences.
     * This is required because Editors are immutable and bound to specific preference instances.
     */
    override fun createPreferencesEditor(
        publication: Publication,
        preferences: ReaderPreferences,
    ): ReaderPreferencesEditor? {
        return when (preferences) {
            is ReaderPreferences.ReflowableWeb -> {
                ReflowableWebRenditionFactory(
                    application = application,
                    publication = publication,
                    configuration = ReflowableWebConfiguration(),
                )?.createPreferencesEditor(initialPreferences = preferences.value)
                    ?.let { ReaderPreferencesEditor.ReflowableWeb(editor = it) }
            }

            is ReaderPreferences.FixedWeb -> {
                FixedWebRenditionFactory(
                    application = application,
                    publication = publication,
                    configuration = FixedWebConfiguration(),
                )?.createPreferencesEditor(initialPreferences = preferences.value)
                    ?.let { ReaderPreferencesEditor.FixedWeb(editor = it) }
            }

            is ReaderPreferences.Pdf -> {
                val editor = pdfNavigatorFactoryWrapper.createPreferencesEditor(
                    publication = publication,
                    initialPreferences = preferences.value,
                )
                ReaderPreferencesEditor.Pdf(editor = editor)
            }

            is ReaderPreferences.Audio -> {
                audioNavigatorFactory.createPreferencesEditor(
                    publication = publication,
                    initialPreferences = preferences.value,
                )?.let {
                    @Suppress("UNCHECKED_CAST")
                    val audioEditor = it
                    ReaderPreferencesEditor.Audio(editor = audioEditor)
                }
            }

            else -> null
        }
    }

    /**
     * Loads and deserializes preferences for the given book and profile.
     */
    override suspend fun loadPreferences(
        bookId: Long,
        publication: Publication,
    ): ReaderPreferences {
        val json = bookPreferencesRepository.getPreferences(bookId = bookId)

        return if (publication.conformsTo(profile = Publication.Profile.EPUB)) {
            val isFixedLayout =
                publication.metadata.layout == Layout.FIXED
            if (isFixedLayout) {
                json?.let {
                    ReaderPreferences.FixedWeb(
                        value = preferencesSerializerFactory.createFixedWebSerializer()
                            .deserialize(preferences = it),
                    )
                } ?: ReaderPreferences.FixedWeb(value = FixedWebPreferences())
            } else {
                json?.let {
                    ReaderPreferences.ReflowableWeb(
                        value = preferencesSerializerFactory.createReflowableWebSerializer()
                            .deserialize(preferences = it),
                    )
                } ?: ReaderPreferences.ReflowableWeb(value = ReflowableWebPreferences())
            }
        } else if (publication.conformsTo(profile = Publication.Profile.PDF)) {
            json?.let {
                ReaderPreferences.Pdf(
                    value = preferencesSerializerFactory.createPdfiumSerializer()
                        .deserialize(preferences = it),
                )
            } ?: ReaderPreferences.Pdf(value = PdfiumPreferences())
        } else {
            // Fallback
            ReaderPreferences.ReflowableWeb(value = ReflowableWebPreferences())
        }
    }

    override suspend fun loadAudiobookPreferences(bookId: Long): ExoPlayerPreferences {
        val json = bookPreferencesRepository.getAudiobookPreferences(bookId = bookId)
        return json?.let {
            preferencesSerializerFactory.createExoPlayerSerializer().deserialize(preferences = it)
        }
            ?: ExoPlayerPreferences()
    }

    /**
     * Builds the TTS Settings Session (Voices, Languages, Editor).
     */
    override suspend fun createTtsSettingsSession(
        bookId: Long,
        publication: Publication,
        ttsManager: ReaderTtsManager,
        application: Application,
    ): TtsSettingsSession? {
        val json = bookPreferencesRepository.getTtsPreferences(bookId = bookId)
        val preferences =
            json?.let {
                preferencesSerializerFactory.createAndroidTtsSerializer()
                    .deserialize(preferences = it)
            }
                ?: AndroidTtsPreferences()

        val factory = ttsNavigatorFactoryWrapper.createFactory(
            application = application,
            publication = publication,
        ) ?: return null
        val editor = factory.createPreferencesEditor(preferences = preferences)

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
    override fun refreshSessionState(
        currentState: ReaderUiState,
        newPreferences: ReaderPreferences,
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

                currentState.copy(preferencesEditor = newEditor)
            }

            else -> null
        }
    }
}
