package com.example.readiumandroidtestapp.features.reader.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.data.repository.BookRepository
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset

@RunWith(AndroidJUnit4::class)
class AudiobookPlaybackLauncherTest {

    private val bookRepository: BookRepository = mockk()
    private val openPublicationUseCase: OpenPublicationUseCase = mockk()
    private val readerSessionFactory: ReaderSessionFactory = mockk()
    private val audioPlaybackManager: AudioPlaybackManager = mockk(relaxed = true)

    private val bookFlow = MutableStateFlow<Book?>(null)
    private val navigatorFlow =
        MutableStateFlow<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>?>(null)
    private val publicationFlow = MutableStateFlow<Publication?>(null)

    private lateinit var launcher: AudiobookPlaybackLauncher

    @Before
    fun setUp() {
        every { audioPlaybackManager.book } returns bookFlow
        every { audioPlaybackManager.navigator } returns navigatorFlow
        every { audioPlaybackManager.publication } returns publicationFlow

        launcher = AudiobookPlaybackLauncher(
            bookRepository = bookRepository,
            openPublicationUseCase = openPublicationUseCase,
            readerSessionFactory = readerSessionFactory,
            audioPlaybackManager = audioPlaybackManager,
        )
    }

    @Test
    fun `launchPlayback returns failure when book not found`() = runTest {
        coEvery { bookRepository.get(bookId = 1L) } returns null

        val result = launcher.launchPlayback(bookId = 1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `launchPlayback resumes active book when already loaded`() = runTest {
        val book = mockk<Book> {
            every { id } returns 1L
        }
        val navigator =
            mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>(relaxed = true)
        bookFlow.value = book
        navigatorFlow.value = navigator

        val result = launcher.launchPlayback(bookId = 1L)

        assertTrue(result.isSuccess)
        verify { navigator.play() }
    }

    @Test
    fun `launchPlayback opens publication and starts playback for new book`() = runTest {
        val url = AbsoluteUrl(url = "file:///test.audiobook")!!
        val book = mockk<Book> {
            every { id } returns 1L
            every { this@mockk.url } returns url
        }
        val asset = mockk<Asset>(relaxed = true)
        val publication = mockk<Publication>(relaxed = true)
        val openedBook = OpenedBook(publication = publication, asset = asset)
        val navigator =
            mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>(relaxed = true)
        val audioState = ReaderUiState.Audio(
            publication = publication,
            book = book,
            navigator = navigator,
            preferencesEditor = null,
        )

        bookFlow.value = null
        navigatorFlow.value = null
        coEvery { bookRepository.get(bookId = 1L) } returns book
        coEvery { openPublicationUseCase(url = url) } returns Result.success(value = openedBook)
        coEvery {
            readerSessionFactory.createAudioSession(
                book = book,
                publication = publication,
            )
        } returns Result.success(value = audioState)

        val result = launcher.launchPlayback(bookId = 1L)

        assertTrue(result.isSuccess)
        verify {
            audioPlaybackManager.load(
                book = book,
                publication = publication,
                asset = asset,
                audioNavigator = navigator,
                editor = null,
            )
        }
        verify { navigator.play() }
    }
}
