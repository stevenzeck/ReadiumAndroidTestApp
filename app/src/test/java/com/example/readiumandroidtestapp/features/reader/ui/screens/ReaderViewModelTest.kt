package com.example.readiumandroidtestapp.features.reader.ui.screens

import android.app.Application
import com.example.readiumandroidtestapp.core.data.book.BookRepository
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.domain.OpenPublicationUseCase
import com.example.readiumandroidtestapp.features.reader.domain.OpenedBook
import com.example.readiumandroidtestapp.features.reader.domain.ReaderDecorationManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderPreferencesManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderSessionFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.ReaderMediaBinder
import com.example.readiumandroidtestapp.features.reader.ui.search.ReaderSearchManager
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderError
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private val application: Application = mockk(relaxed = true)
    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val openPublicationUseCase: OpenPublicationUseCase = mockk()
    private val searchManager: ReaderSearchManager = mockk(relaxed = true)
    private val ttsManager: ReaderTtsManager = mockk(relaxed = true)
    private val preferencesManager: ReaderPreferencesManager = mockk(relaxed = true)
    private val decorationManager: ReaderDecorationManager = mockk(relaxed = true)
    private val sessionFactory: ReaderSessionFactory = mockk()
    private val mediaBinder: ReaderMediaBinder = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Setup default mocks for flows to prevent initialization crashes
        every { bookRepository.bookmarksForBook(bookId = any()) } returns flowOf(value = emptyList())
        every { bookRepository.highlightsForBook(bookId = any()) } returns flowOf(value = emptyList())
        every { decorationManager.showHighlightDialog } returns MutableStateFlow(value = false)
        every { searchManager.searchQuery } returns MutableStateFlow(value = null)
        every { ttsManager.isTtsActive } returns MutableStateFlow(value = false)
        every { ttsManager.ttsPlayback } returns flowOf(value = false)
        every { searchManager.searchDecorations } returns flowOf(value = emptyList())
        every { decorationManager.decorationFlow(bookId = any()) } returns flowOf(value = emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBookData success updates uiState to Visual`() = runTest {
        val bookId = 1L
        val book = mockk<Book>(relaxed = true)
        val url = mockk<AbsoluteUrl>()
        every { book.url } returns url

        coEvery { bookRepository.get(bookId = bookId) } returns book

        val publication = mockk<Publication>(relaxed = true)
        val asset = mockk<Asset>(relaxed = true)
        val openedBook = OpenedBook(publication = publication, asset = asset)

        coEvery { openPublicationUseCase.invoke(url = url) } returns Result.success(value = openedBook)

        val visualState = mockk<ReaderUiState.Visual>(relaxed = true)
        coEvery {
            sessionFactory.createVisualSession(
                book = book,
                publication = publication,
            )
        } returns visualState

        val viewModel = ReaderViewModel(
            application = application,
            bookRepository = bookRepository,
            openPublicationUseCase = openPublicationUseCase,
            searchManager = searchManager,
            ttsManager = ttsManager,
            preferencesManager = preferencesManager,
            decorationManager = decorationManager,
            sessionFactory = sessionFactory,
            mediaBinder = mediaBinder,
            bookId = bookId,
        )

        advanceUntilIdle()

        Assert.assertEquals(visualState, viewModel.uiState.value)
    }

    @Test
    fun `loadBookData failure due to missing book returns Error`() = runTest {
        val bookId = 1L
        coEvery { bookRepository.get(bookId = bookId) } returns null

        val viewModel = ReaderViewModel(
            application = application,
            bookRepository = bookRepository,
            openPublicationUseCase = openPublicationUseCase,
            searchManager = searchManager,
            ttsManager = ttsManager,
            preferencesManager = preferencesManager,
            decorationManager = decorationManager,
            sessionFactory = sessionFactory,
            mediaBinder = mediaBinder,
            bookId = bookId,
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        Assert.assertTrue(state is ReaderUiState.Error)
        Assert.assertEquals(ReaderError.InvalidBookLocation, (state as ReaderUiState.Error).error)
    }

    @Test
    fun `loadBookData failure due to open exception returns Error`() = runTest {
        val bookId = 1L
        val book = mockk<Book>(relaxed = true)
        val url = mockk<AbsoluteUrl>()
        every { book.url } returns url

        coEvery { bookRepository.get(bookId = bookId) } returns book

        coEvery { openPublicationUseCase.invoke(url = url) } returns Result.failure(Exception("Open failed"))

        val viewModel = ReaderViewModel(
            application = application,
            bookRepository = bookRepository,
            openPublicationUseCase = openPublicationUseCase,
            searchManager = searchManager,
            ttsManager = ttsManager,
            preferencesManager = preferencesManager,
            decorationManager = decorationManager,
            sessionFactory = sessionFactory,
            mediaBinder = mediaBinder,
            bookId = bookId,
        )

        advanceUntilIdle()

        val state = viewModel.uiState.value
        Assert.assertTrue(state is ReaderUiState.Error)
        Assert.assertTrue((state as ReaderUiState.Error).error is ReaderError.PublicationOpenFailed)
    }
}
