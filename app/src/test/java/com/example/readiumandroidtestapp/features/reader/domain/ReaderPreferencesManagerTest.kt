package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.features.reader.data.BookPreferencesRepository
import com.example.readiumandroidtestapp.features.reader.data.PreferencesSerializerFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerEngineProvider
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesEditor
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesSerializer
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumSettings
import org.readium.navigator.media.audio.AudioNavigatorFactory
import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesSerializer
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.epub.EpubPreferencesEditor
import org.readium.r2.navigator.epub.EpubPreferencesSerializer
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

@RunWith(AndroidJUnit4::class)
class ReaderPreferencesManagerTest {

    private val bookPreferencesRepository: BookPreferencesRepository = mockk(relaxed = true)
    private val preferencesSerializerFactory: PreferencesSerializerFactory = mockk(relaxed = true)
    private val applicationContext = mockk<Application>(relaxed = true)

    private lateinit var manager: ReaderPreferencesManager

    private val epubSerializer = mockk<EpubPreferencesSerializer>(relaxed = true)
    private val pdfSerializer = mockk<PdfiumPreferencesSerializer>(relaxed = true)
    private val ttsSerializer = mockk<AndroidTtsPreferencesSerializer>(relaxed = true)
    private val audioSerializer = mockk<ExoPlayerPreferencesSerializer>(relaxed = true)

    @Before
    fun setUp() {
        mockkConstructor(EpubNavigatorFactory::class)
        mockkConstructor(PdfNavigatorFactory::class)
        mockkConstructor(AudioNavigatorFactory::class)
        mockkConstructor(AndroidTtsNavigatorFactory::class)
        mockkConstructor(ExoPlayerEngineProvider::class)

        every { preferencesSerializerFactory.createEpubSerializer() } returns epubSerializer
        every { preferencesSerializerFactory.createPdfiumSerializer() } returns pdfSerializer
        every { preferencesSerializerFactory.createAndroidTtsSerializer() } returns ttsSerializer
        every { preferencesSerializerFactory.createExoPlayerSerializer() } returns audioSerializer

        manager = ReaderPreferencesManager(
            applicationContext = applicationContext,
            bookPreferencesRepository = bookPreferencesRepository,
            preferencesSerializerFactory = preferencesSerializerFactory,
        )
    }

    @After
    fun tearDown() {
        unmockkConstructor(EpubNavigatorFactory::class)
        unmockkConstructor(PdfNavigatorFactory::class)
        unmockkConstructor(AudioNavigatorFactory::class)
        unmockkConstructor(AndroidTtsNavigatorFactory::class)
        unmockkConstructor(ExoPlayerEngineProvider::class)
    }

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

    @Test
    fun `createPreferencesEditor for EPUB`() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = any()) } returns true
        val preferences = EpubPreferences()
        val editor = mockk<EpubPreferencesEditor>()

        every {
            anyConstructed<EpubNavigatorFactory>().createPreferencesEditor(
                currentPreferences = any(),
            )
        } returns editor

        val result =
            manager.createPreferencesEditor(publication = publication, preferences = preferences)

        assertEquals(editor, result)
    }

    @Test
    fun `createPreferencesEditor for PDF`() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(any()) } returns true
        val preferences = PdfiumPreferences()
        val editor = mockk<PreferencesEditor<PdfiumPreferences>>()

        every {
            anyConstructed<PdfNavigatorFactory<PdfiumSettings, PdfiumPreferences, PreferencesEditor<PdfiumPreferences>>>().createPreferencesEditor(
                initialPreferences = any(),
            )
        } returns editor

        val result =
            manager.createPreferencesEditor(publication = publication, preferences = preferences)

        assertEquals(editor, result)
    }

    @Test
    fun `createPreferencesEditor for Audio`() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = any()) } returns true
        val audioLink = Link(href = Url("test.mp3")!!, mediaType = MediaType(string = "audio/mpeg"))
        every { publication.readingOrder } returns listOf(audioLink)
        every { publication.links } returns listOf(audioLink)
        val preferences = ExoPlayerPreferences()
        val editor = mockk<ExoPlayerPreferencesEditor>()

        every {
            anyConstructed<AudioNavigatorFactory<ExoPlayerSettings, ExoPlayerPreferences, ExoPlayerPreferencesEditor>>().createAudioPreferencesEditor(
                currentPreferences = any<ExoPlayerPreferences>(),
            )
        } returns editor

        val result =
            manager.createPreferencesEditor(publication = publication, preferences = preferences)

        assertEquals(editor, result)
    }

    @Test
    fun `createPreferencesEditor returns null for unknown preferences`() {
        val publication = mockk<Publication>(relaxed = true)
        val preferences = mockk<Configurable.Preferences<*>>()

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
    fun `loadAudiobookPreferences returns deserialized preferences`() = runTest {
        val bookId = 1L
        val json = "{}"
        val preferences = ExoPlayerPreferences()

        coEvery { bookPreferencesRepository.getAudiobookPreferences(bookId = bookId) } returns json
        every { audioSerializer.deserialize(preferences = json) } returns preferences

        val result = manager.loadAudiobookPreferences(bookId = bookId)

        assertEquals(preferences, result)
    }
}
