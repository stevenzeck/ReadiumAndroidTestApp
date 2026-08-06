package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.model.Book
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.unmockkConstructor
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerEngineProvider
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesEditor
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.audio.AudioNavigatorFactory
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

@RunWith(AndroidJUnit4::class)
class ReaderSessionFactoryTest {

    private val preferencesManager: ReaderPreferencesManager = mockk(relaxed = true)
    private val pdfiumDocumentFactory: PdfiumDocumentFactory = mockk(relaxed = true)
    private val applicationContext = ApplicationProvider.getApplicationContext<Application>()

    private val factory = ReaderSessionFactory(
        applicationContext = applicationContext,
        preferencesManager = preferencesManager,
        pdfiumDocumentFactory = pdfiumDocumentFactory,
    )

    private val realBook = Book(
        id = 1L,
        href = "href",
        title = "Title",
        identifier = "id",
        mediaType = MediaType.EPUB,
        cover = null,
    )

    @Before
    fun setup() {
        mockkConstructor(AudioNavigatorFactory::class)
        mockkConstructor(ExoPlayerEngineProvider::class)
    }

    @After
    fun teardown() {
        unmockkConstructor(AudioNavigatorFactory::class)
        unmockkConstructor(ExoPlayerEngineProvider::class)
    }

    @Test
    fun `createVisualSession returns correct state`() = runTest {
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = any()) } returns true
        }

        val preferences = mockk<Configurable.Preferences<*>>()
        coEvery {
            preferencesManager.loadPreferences(
                bookId = any(),
                publication = any(),
            )
        } returns preferences
        every {
            preferencesManager.createPreferencesEditor(
                publication = any(),
                preferences = any(),
            )
        } returns mockk()

        val result = factory.createVisualSession(book = realBook, publication = publication)

        assertEquals(publication, result.publication)
        assertEquals(realBook, result.book)
        assertEquals(pdfiumDocumentFactory, result.pdfiumDocumentFactory)
        assertTrue(result.capabilities.canSpeak)
    }

    @Test
    fun `createAudioSession returns success when navigator creation succeeds`() = runTest {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = any()) } returns true
        val audioLink = Link(href = Url("test.mp3")!!, mediaType = MediaType(string = "audio/mpeg"))
        every { publication.readingOrder } returns listOf(audioLink)
        every { publication.links } returns listOf(audioLink)
        val navigator = mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>()

        coEvery { preferencesManager.loadAudiobookPreferences(bookId = any()) } returns ExoPlayerPreferences()

        coEvery {
            anyConstructed<AudioNavigatorFactory<ExoPlayerSettings, ExoPlayerPreferences, ExoPlayerPreferencesEditor>>().createNavigator(
                initialLocator = null,
                initialPreferences = any<ExoPlayerPreferences>(),
                readingOrder = any(),
            )
        } returns Try.success(success = navigator)
        every {
            anyConstructed<AudioNavigatorFactory<ExoPlayerSettings, ExoPlayerPreferences, ExoPlayerPreferencesEditor>>().createAudioPreferencesEditor(
                currentPreferences = any<ExoPlayerPreferences>(),
            )
        } returns mockk()

        val result = factory.createAudioSession(book = realBook, publication = publication)

        assertTrue(result.isSuccess)
        assertEquals(navigator, result.getOrThrow().navigator)
    }
}
