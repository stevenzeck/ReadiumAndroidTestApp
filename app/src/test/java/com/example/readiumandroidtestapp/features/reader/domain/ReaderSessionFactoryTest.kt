package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.mediatype.MediaType

class ReaderSessionFactoryTest {

    private val preferencesManager: ReaderPreferencesManager = mockk(relaxed = true)
    private val audioNavigatorFactory: AppAudioNavigatorFactory = mockk()
    private val pdfiumDocumentFactory: PdfiumDocumentFactory = mockk(relaxed = true)

    private val factory = DefaultReaderSessionFactory(
        preferencesManager = preferencesManager,
        audioNavigatorFactory = audioNavigatorFactory,
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
        val navigator = mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>()

        coEvery { preferencesManager.loadAudiobookPreferences(bookId = any()) } returns ExoPlayerPreferences()
        coEvery {
            audioNavigatorFactory.createNavigator(
                publication = publication,
                initialLocator = null,
                initialPreferences = any(),
            )
        } returns Try.success(success = navigator)
        every {
            audioNavigatorFactory.createPreferencesEditor(
                publication = any(),
                initialPreferences = any(),
            )
        } returns mockk()

        val result = factory.createAudioSession(book = realBook, publication = publication)

        assertTrue(result.isSuccess)
        assertEquals(navigator, result.getOrThrow().navigator)
    }
}
