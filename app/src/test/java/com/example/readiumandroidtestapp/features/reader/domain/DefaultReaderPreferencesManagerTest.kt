package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import com.example.readiumandroidtestapp.features.reader.data.AndroidTtsNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.BookPreferencesRepository
import com.example.readiumandroidtestapp.features.reader.data.EpubNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.PdfNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.PreferencesSerializerFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesEditor
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesSerializer
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.epub.EpubPreferencesEditor
import org.readium.r2.navigator.epub.EpubPreferencesSerializer
import org.readium.r2.navigator.epub.EpubSettings
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Language

class DefaultReaderPreferencesManagerTest {

    private val bookPreferencesRepository: BookPreferencesRepository = mockk(relaxed = true)
    private val audioNavigatorFactory: AppAudioNavigatorFactory = mockk(relaxed = true)
    private val ttsNavigatorFactoryWrapper: AndroidTtsNavigatorFactoryWrapper =
        mockk(relaxed = true)
    private val epubNavigatorFactoryWrapper: EpubNavigatorFactoryWrapper = mockk(relaxed = true)
    private val pdfNavigatorFactoryWrapper: PdfNavigatorFactoryWrapper = mockk(relaxed = true)
    private val preferencesSerializerFactory: PreferencesSerializerFactory = mockk(relaxed = true)
    private val application: Application = mockk()
    private val ttsManager: ReaderTtsManager = mockk(relaxed = true)

    private lateinit var manager: DefaultReaderPreferencesManager

    private val epubSerializer = mockk<EpubPreferencesSerializer>(relaxed = true)
    private val pdfSerializer = mockk<PdfiumPreferencesSerializer>(relaxed = true)
    private val ttsSerializer = mockk<AndroidTtsPreferencesSerializer>(relaxed = true)
    private val audioSerializer = mockk<ExoPlayerPreferencesSerializer>(relaxed = true)

    @Before
    fun setUp() {
        every { preferencesSerializerFactory.createEpubSerializer() } returns epubSerializer
        every { preferencesSerializerFactory.createPdfiumSerializer() } returns pdfSerializer
        every { preferencesSerializerFactory.createAndroidTtsSerializer() } returns ttsSerializer
        every { preferencesSerializerFactory.createExoPlayerSerializer() } returns audioSerializer

        manager = DefaultReaderPreferencesManager(
            bookPreferencesRepository = bookPreferencesRepository,
            audioNavigatorFactory = audioNavigatorFactory,
            ttsNavigatorFactoryWrapper = ttsNavigatorFactoryWrapper,
            epubNavigatorFactoryWrapper = epubNavigatorFactoryWrapper,
            pdfNavigatorFactoryWrapper = pdfNavigatorFactoryWrapper,
            preferencesSerializerFactory = preferencesSerializerFactory,
        )
    }

    /**
     * A simple fake implementation of [Preference] to avoid mocking.
     * @param _value The current value (nullable).
     * @param _effectiveValue The effective value (non-nullable fallback).
     */
    class FakePreference<T>(
        private var _value: T?,
        private val _effectiveValue: T,
    ) : Preference<T> {
        override val value: T? get() = _value
        override val effectiveValue: T get() = _effectiveValue
        override val isEffective: Boolean = true
        override fun set(value: T?) {
            _value = value
        }
    }

    interface ConfigurableVisualNavigator : VisualNavigator,
        Configurable<EpubSettings, EpubPreferences>

    interface ConfigurablePdfNavigator : VisualNavigator,
        Configurable<PdfiumSettings, PdfiumPreferences>

    class UnknownPreferences : Configurable.Preferences<UnknownPreferences> {
        override fun plus(other: UnknownPreferences): UnknownPreferences = this
    }

    @Test
    fun `commitPreferences ignores unknown preferences`() = runTest {
        val bookId = 1L
        val preferences = UnknownPreferences()

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            currentVisualNavigator = null,
            audioNavigator = null,
            ttsManager = ttsManager,
        )

        // Verify nothing was saved
        coVerify(exactly = 0) {
            bookPreferencesRepository.savePreferences(
                bookId = any(),
                preferencesJson = any(),
            )
        }
        coVerify(exactly = 0) {
            bookPreferencesRepository.saveTtsPreferences(
                bookId = any(),
                preferencesJson = any(),
            )
        }
        coVerify(exactly = 0) {
            bookPreferencesRepository.saveAudiobookPreferences(
                bookId = any(),
                preferencesJson = any(),
            )
        }
    }

    @Test
    fun `commitPreferences saves epub preferences`() = runTest {
        val bookId = 1L
        val preferences = EpubPreferences()
        val visualNavigator = mockk<ConfigurableVisualNavigator>(relaxed = true)
        val json = "{}"
        every { epubSerializer.serialize(preferences = preferences) } returns json

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            currentVisualNavigator = visualNavigator,
            audioNavigator = null,
            ttsManager = ttsManager,
        )

        verify { visualNavigator.submitPreferences(preferences = preferences) }
        coVerify {
            bookPreferencesRepository.savePreferences(
                bookId = bookId,
                preferencesJson = json,
            )
        }
    }

    @Test
    fun `commitPreferences saves pdf preferences`() = runTest {
        val bookId = 1L
        val preferences = PdfiumPreferences()
        val visualNavigator = mockk<ConfigurablePdfNavigator>(relaxed = true)
        val json = "{}"
        every { pdfSerializer.serialize(preferences = preferences) } returns json

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            currentVisualNavigator = visualNavigator,
            audioNavigator = null,
            ttsManager = ttsManager,
        )

        verify { visualNavigator.submitPreferences(preferences = preferences) }
        coVerify {
            bookPreferencesRepository.savePreferences(
                bookId = bookId,
                preferencesJson = json,
            )
        }
    }

    @Test
    fun `commitPreferences saves tts preferences`() = runTest {
        val bookId = 1L
        val preferences = AndroidTtsPreferences()
        val json = "{}"
        every { ttsSerializer.serialize(preferences = preferences) } returns json

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            currentVisualNavigator = null,
            audioNavigator = null,
            ttsManager = ttsManager,
        )

        verify { ttsManager.submitPreferences(preferences = preferences) }
        coVerify {
            bookPreferencesRepository.saveTtsPreferences(
                bookId = bookId,
                preferencesJson = json,
            )
        }
    }

    @Test
    fun `commitPreferences saves audio preferences`() = runTest {
        val bookId = 1L
        val preferences = ExoPlayerPreferences()
        val audioNavigator = mockk<AudioNavigator<*, ExoPlayerPreferences>>(relaxed = true)
        val json = "{}"
        every { audioSerializer.serialize(preferences = preferences) } returns json

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            currentVisualNavigator = null,
            audioNavigator = audioNavigator,
            ttsManager = ttsManager,
        )

        verify {
            (audioNavigator as Configurable<*, ExoPlayerPreferences>).submitPreferences(
                preferences = preferences,
            )
        }
        coVerify {
            bookPreferencesRepository.saveAudiobookPreferences(
                bookId = bookId,
                preferencesJson = json,
            )
        }
    }

    @Test
    fun `createPreferencesEditor for EPUB`() {
        val publication = mockk<Publication>()
        val preferences = EpubPreferences()
        val editor = mockk<EpubPreferencesEditor>()

        every {
            epubNavigatorFactoryWrapper.createPreferencesEditor(
                publication = publication,
                initialPreferences = preferences,
            )
        } returns editor

        val result =
            manager.createPreferencesEditor(publication = publication, preferences = preferences)

        assertEquals(editor, result)
    }

    @Test
    fun `createPreferencesEditor for PDF`() {
        val publication = mockk<Publication>()
        val preferences = PdfiumPreferences()
        val editor = mockk<PreferencesEditor<PdfiumPreferences>>()

        every {
            pdfNavigatorFactoryWrapper.createPreferencesEditor(
                publication = publication,
                initialPreferences = preferences,
            )
        } returns editor

        val result =
            manager.createPreferencesEditor(publication = publication, preferences = preferences)

        assertEquals(editor, result)
    }

    @Test
    fun `createPreferencesEditor for Audio`() {
        val publication = mockk<Publication>()
        val preferences = ExoPlayerPreferences()
        val editor = mockk<PreferencesEditor<ExoPlayerPreferences>>()

        every {
            audioNavigatorFactory.createPreferencesEditor(
                publication = publication,
                initialPreferences = preferences,
            )
        } returns editor

        val result =
            manager.createPreferencesEditor(publication = publication, preferences = preferences)

        assertEquals(editor, result)
    }

    @Test
    fun `createPreferencesEditor returns null for unknown preferences`() {
        val publication = mockk<Publication>()
        val preferences = UnknownPreferences()

        val result =
            manager.createPreferencesEditor(publication = publication, preferences = preferences)

        assertNull(result)
    }

    @Test
    fun `loadPreferences for EPUB`() = runTest {
        val bookId = 1L
        val publication = mockk<Publication> {
            every { conformsTo(profile = Publication.Profile.EPUB) } returns true
        }
        val json = "{}"
        val preferences = EpubPreferences()

        coEvery { bookPreferencesRepository.getPreferences(bookId = bookId) } returns json
        every { epubSerializer.deserialize(preferences = json) } returns preferences

        val result = manager.loadPreferences(bookId = bookId, publication = publication)

        assertEquals(preferences, result)
    }

    @Test
    fun `loadPreferences for PDF`() = runTest {
        val bookId = 1L
        val publication = mockk<Publication> {
            every { conformsTo(profile = Publication.Profile.EPUB) } returns false
            every { conformsTo(profile = Publication.Profile.PDF) } returns true
        }
        val json = "{}"
        val preferences = PdfiumPreferences()

        coEvery { bookPreferencesRepository.getPreferences(bookId = bookId) } returns json
        every { pdfSerializer.deserialize(preferences = json) } returns preferences

        val result = manager.loadPreferences(bookId = bookId, publication = publication)

        assertEquals(preferences, result)
    }

    @Test
    fun `loadPreferences returns default EpubPreferences for unknown publication type`() = runTest {
        val bookId = 1L
        val publication = mockk<Publication> {
            every { conformsTo(profile = Publication.Profile.EPUB) } returns false
            every { conformsTo(profile = Publication.Profile.PDF) } returns false
        }

        coEvery { bookPreferencesRepository.getPreferences(bookId = bookId) } returns "{}"

        val result = manager.loadPreferences(bookId = bookId, publication = publication)

        assertEquals(EpubPreferences(), result)
    }

    @Test
    fun `loadAudiobookPreferences returns deserialized preferences`() = runTest {
        val bookId = 1L
        val json = "{}"
        val preferences = ExoPlayerPreferences()

        coEvery { bookPreferencesRepository.getAudiobookPreferences(bookId = bookId) } returns json
        every { audioSerializer.deserialize(preferences = json) } returns preferences

        val result = manager.loadAudiobookPreferences(bookId = bookId)

        assertEquals(preferences, result)
    }

    @Test
    fun `loadAudiobookPreferences returns empty preferences when repo returns null`() = runTest {
        val bookId = 1L
        coEvery { bookPreferencesRepository.getAudiobookPreferences(bookId = bookId) } returns null

        val result = manager.loadAudiobookPreferences(bookId = bookId)

        assertEquals(ExoPlayerPreferences(), result)
    }

    @Test
    fun `createTtsSettingsSession returns session on success`() = runTest {
        val bookId = 1L
        val publication = mockk<Publication>(relaxed = true)
        val factory = mockk<AndroidTtsNavigatorFactory>(relaxed = true)
        val editor = mockk<AndroidTtsPreferencesEditor>(relaxed = true)
        val preferences = AndroidTtsPreferences()

        coEvery {
            ttsNavigatorFactoryWrapper.createFactory(
                application = application,
                publication = publication,
            )
        } returns factory
        coEvery { bookPreferencesRepository.getTtsPreferences(bookId = bookId) } returns "{}"
        every { ttsSerializer.deserialize(preferences = "{}") } returns preferences
        every { factory.createPreferencesEditor(preferences = preferences) } returns editor

        val fakeLanguage = FakePreference<Language?>(_value = null, _effectiveValue = null)
        every { editor.language } returns fakeLanguage

        val fakeVoices = FakePreference<Map<Language, AndroidTtsEngine.Voice.Id>>(
            _value = emptyMap(),
            _effectiveValue = emptyMap(),
        )
        every { editor.voices } returns fakeVoices

        every { ttsManager.voices } returns emptySet()

        val session = manager.createTtsSettingsSession(
            bookId = bookId,
            publication = publication,
            ttsManager = ttsManager,
            application = application,
        )

        assertNotNull(session)
    }

    @Test
    fun `createTtsSettingsSession voice preference logic`() = runTest {
        val bookId = 1L
        val publication = mockk<Publication>(relaxed = true)
        val factory = mockk<AndroidTtsNavigatorFactory>(relaxed = true)
        val editor = mockk<AndroidTtsPreferencesEditor>(relaxed = true)
        val preferences = AndroidTtsPreferences()

        coEvery {
            ttsNavigatorFactoryWrapper.createFactory(
                application = application,
                publication = publication,
            )
        } returns factory
        coEvery { bookPreferencesRepository.getTtsPreferences(bookId = bookId) } returns "{}"
        every { ttsSerializer.deserialize(preferences = "{}") } returns preferences
        every { factory.createPreferencesEditor(preferences = preferences) } returns editor

        // Setup voices
        val enVoice = AndroidTtsEngine.Voice(
            id = AndroidTtsEngine.Voice.Id(value = "en-id"),
            language = Language(code = "en"),
            quality = AndroidTtsEngine.Voice.Quality.Normal,
            requiresNetwork = false,
        )
        val frVoice = AndroidTtsEngine.Voice(
            id = AndroidTtsEngine.Voice.Id(value = "fr-id"),
            language = Language(code = "fr"),
            quality = AndroidTtsEngine.Voice.Quality.Normal,
            requiresNetwork = false,
        )
        every { ttsManager.voices } returns setOf(enVoice, frVoice)

        val fakeLanguagePref = FakePreference<Language?>(
            _value = Language(code = "en"),
            _effectiveValue = Language(code = "en"),
        )
        every { editor.language } returns fakeLanguagePref

        val underlyingVoicesMap = mapOf(Language(code = "en") to enVoice.id)
        val fakeVoicesPref = FakePreference(
            _value = underlyingVoicesMap,
            _effectiveValue = emptyMap(),
        )
        every { editor.voices } returns fakeVoicesPref

        val session = manager.createTtsSettingsSession(
            bookId = bookId,
            publication = publication,
            ttsManager = ttsManager,
            application = application,
        )!!

        val selectedVoice = session.voice.value
        assertEquals(enVoice, selectedVoice)

        // Perform Action
        session.voice.set(frVoice)

        val updatedMap = fakeVoicesPref.value
        assertNotNull(updatedMap)
        assertEquals(frVoice.id, updatedMap?.get(Language(code = "en")))
    }

    @Test
    fun `createTtsSettingsSession returns null when factory is null`() = runTest {
        val bookId = 1L
        val publication = mockk<Publication>(relaxed = true)

        coEvery {
            ttsNavigatorFactoryWrapper.createFactory(
                application = application,
                publication = publication,
            )
        } returns null

        val session = manager.createTtsSettingsSession(
            bookId = bookId,
            publication = publication,
            ttsManager = ttsManager,
            application = application,
        )

        assertNull(session)
    }

    @Test
    fun `refreshSessionState refreshes Visual state`() {
        val publication = mockk<Publication>()
        val preferences = EpubPreferences()
        val editor = mockk<PreferencesEditor<EpubPreferences>>()
        val currentState = ReaderUiState.Visual(
            publication = publication,
            book = mockk(),
            initialLocator = null,
            pdfiumDocumentFactory = mockk(),
            capabilities = ReaderCapabilities(
                isSearchable = true,
                canSpeak = true,
                hasPreferences = true,
            ),
            initialPreferences = preferences,
        )

        every {
            epubNavigatorFactoryWrapper.createPreferencesEditor(
                publication = publication,
                initialPreferences = preferences,
            )
        } returns editor

        val newState =
            manager.refreshSessionState(currentState = currentState, newPreferences = preferences)

        assertNotNull(newState)
        assertEquals(editor, (newState as ReaderUiState.Visual).preferencesEditor)
    }

    @Test
    fun `refreshSessionState refreshes Audio state`() {
        val publication = mockk<Publication>()
        val preferences = ExoPlayerPreferences()
        val editor = mockk<PreferencesEditor<ExoPlayerPreferences>>()
        val currentState = ReaderUiState.Audio(
            publication = publication,
            book = mockk(),
            navigator = mockk(),
        )

        every {
            audioNavigatorFactory.createPreferencesEditor(
                publication = publication,
                initialPreferences = preferences,
            )
        } returns editor

        val newState =
            manager.refreshSessionState(currentState = currentState, newPreferences = preferences)

        assertNotNull(newState)
        assertEquals(editor, (newState as ReaderUiState.Audio).preferencesEditor)
    }

    @Test
    fun `refreshSessionState returns null for unknown state`() {
        val currentState = ReaderUiState.Loading
        val preferences = EpubPreferences()

        val result =
            manager.refreshSessionState(currentState = currentState, newPreferences = preferences)

        assertNull(result)
    }

    @Test
    fun `refreshSessionState returns null when editor creation fails`() {
        val publication = mockk<Publication>()
        val preferences = EpubPreferences()
        val currentState = ReaderUiState.Visual(
            publication = publication,
            book = mockk(),
            initialLocator = null,
            pdfiumDocumentFactory = mockk(),
            capabilities = ReaderCapabilities(
                isSearchable = true,
                canSpeak = true,
                hasPreferences = true,
            ),
            initialPreferences = preferences,
        )

        // Pass UnknownPreferences to force createPreferencesEditor to return null
        val unknownPreferences = UnknownPreferences()

        val result = manager.refreshSessionState(
            currentState = currentState,
            newPreferences = unknownPreferences,
        )

        assertNull(result)
    }
}
