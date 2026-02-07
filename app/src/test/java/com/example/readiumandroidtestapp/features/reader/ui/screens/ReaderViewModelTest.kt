package com.example.readiumandroidtestapp.features.reader.ui.screens

import android.app.Application
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.features.reader.domain.*
import com.example.readiumandroidtestapp.features.reader.ui.audio.ReaderMediaBinder
import com.example.readiumandroidtestapp.features.reader.ui.search.ReaderSearchManager
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.TtsSettingsSession
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private lateinit var viewModel: ReaderViewModel
    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val openPublicationUseCase: OpenPublicationUseCase = mockk()
    private val sessionFactory: ReaderSessionFactory = mockk(relaxed = true)
    private val searchManager: ReaderSearchManager = mockk(relaxed = true)
    private val ttsManager: ReaderTtsManager = mockk(relaxed = true)
    private val preferencesManager: ReaderPreferencesManager = mockk(relaxed = true)
    private val decorationManager: ReaderDecorationManager = mockk(relaxed = true)
    private val mediaBinder: ReaderMediaBinder = mockk(relaxed = true)
    private val application: Application = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { ttsManager.isTtsActive } returns MutableStateFlow(false)
        every { decorationManager.showHighlightDialog } returns MutableStateFlow(false)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- State Tests ---

    @Test
    fun `initialization loads book and sets Visual state on success`() = runTest(testDispatcher) {
        // GIVEN a book that opens successfully
        val bookId = 2L
        val book = mockk<Book>(relaxed = true)
        val publication = mockk<Publication>(relaxed = true)

        prepareSuccessfulLoad(bookId = bookId, book = book, publication = publication, isVisual = true)

        // WHEN the ViewModel is created
        viewModel = createViewModel(bookId = bookId)
        advanceUntilIdle()

        // THEN the UI state should be Visual
        assertTrue("State should be Visual", viewModel.uiState.value is ReaderUiState.Visual)
    }

    @Test
    fun `initialization sets Error state when loading fails`() = runTest(testDispatcher) {
        // GIVEN a book that fails to load
        val bookId = 1L
        val book = mockk<Book>(relaxed = true)

        coEvery { bookRepository.get(bookId) } returns book
        coEvery { openPublicationUseCase(url = any()) } returns Result.failure(Exception("Network Error"))

        // WHEN the ViewModel is created
        viewModel = createViewModel(bookId = bookId)
        advanceUntilIdle()

        // THEN the UI state should be Error
        assertTrue("State should be Error", viewModel.uiState.value is ReaderUiState.Error)
    }

    @Test
    fun `retryLoad attempts to fetch book data again`() = runTest(testDispatcher) {
        // GIVEN initial failure
        val bookId = 1L
        val book = mockk<Book>(relaxed = true)
        coEvery { bookRepository.get(bookId) } returns book
        coEvery { openPublicationUseCase(url = any()) } returns Result.failure(Exception("Fail"))

        viewModel = createViewModel(bookId = bookId)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ReaderUiState.Error)

        // WHEN retry is called with a fixed repository/usecase
        prepareSuccessfulLoad(bookId = bookId, book = book, publication = mockk(relaxed = true), isVisual = true)
        viewModel.retryLoad()
        advanceUntilIdle()

        // THEN we recover to Visual state
        assertTrue(viewModel.uiState.value is ReaderUiState.Visual)
    }

    // --- Interaction / Logic Tests (Medium Value) ---

    @Test
    fun `openSettings shows Configurable sheet when TTS is inactive`() = runTest(testDispatcher) {
        // GIVEN a visual session with TTS off
        every { ttsManager.isTtsActive } returns MutableStateFlow(false)
        prepareSuccessfulLoad(bookId = 1L, book = mockk(relaxed = true), publication = mockk(relaxed = true), isVisual = true)

        viewModel = createViewModel(bookId = 1L)
        advanceUntilIdle()

        // WHEN settings are opened
        viewModel.openSettings()
        advanceUntilIdle()

        // THEN the specific sheet state is set
        assertTrue(viewModel.settingsSheetState.value is ReaderSettingsSheet.Configurable)
    }

    @Test
    fun `openSettings shows TTS sheet when TTS is active`() = runTest(testDispatcher) {
        // GIVEN a visual session with TTS ON
        every { ttsManager.isTtsActive } returns MutableStateFlow(true)
        prepareSuccessfulLoad(bookId = 1L, book = mockk(relaxed = true), publication = mockk(relaxed = true), isVisual = true)

        viewModel = createViewModel(bookId = 1L)
        advanceUntilIdle()

        // WHEN settings are opened
        val mockSession = mockk<TtsSettingsSession>(relaxed = true)
        coEvery {
            preferencesManager.createTtsSettingsSession(
                bookId = any(),
                publication = any(),
                ttsManager = any(),
                application = any()
            )
        } returns mockSession

        viewModel.openSettings()
        advanceUntilIdle()

        // THEN we get the TTS specific sheet
        assertTrue(viewModel.settingsSheetState.value is ReaderSettingsSheet.Tts)
    }

    @Test
    fun `closeSettings clears the sheet state`() = runTest(testDispatcher) {
        // GIVEN settings are open
        viewModel = createViewModel(bookId = 1L)
        viewModel.openAudiobookSettings()

        // WHEN closed
        viewModel.closeSettings()
        advanceUntilIdle()

        // THEN state is null
        assertNull(viewModel.settingsSheetState.value)
    }

    // --- Helper Methods ---

    private fun prepareSuccessfulLoad(
        bookId: Long,
        book: Book,
        publication: Publication,
        isVisual: Boolean
    ) {
        val bookUrl = mockk<AbsoluteUrl>()
        every { book.url } returns bookUrl

        // Repository returns the book
        coEvery { bookRepository.get(bookId = bookId) } returns book

        // UseCase returns success
        val openedBook = OpenedBook(publication = publication, asset = mockk(relaxed = true))
        coEvery { openPublicationUseCase(url = bookUrl) } returns Result.success(openedBook)

        // SessionFactory returns the correct UI State type
        if (isVisual) {
            val visualState = ReaderUiState.Visual(
                publication = publication,
                book = book,
                initialLocator = null,
                pdfiumDocumentFactory = mockk(),
                capabilities = mockk(),
                preferencesEditor = null,
                initialPreferences = mockk(),
                isFixedLayout = false
            )
            coEvery { sessionFactory.createVisualSession(book = book, publication) } returns visualState
        } else {
            val audioState = ReaderUiState.Audio(
                publication = publication,
                book = book,
                navigator = mockk(relaxed = true),
                preferencesEditor = mockk(relaxed = true)
            )
            coEvery { sessionFactory.createAudioSession(book = book, publication) } returns Result.success(audioState)
        }
    }

    private fun createViewModel(bookId: Long): ReaderViewModel {
        return ReaderViewModel(
            application = application,
            bookRepository = bookRepository,
            openPublicationUseCase = openPublicationUseCase,
            searchManager = searchManager,
            ttsManager = ttsManager,
            preferencesManager = preferencesManager,
            decorationManager = decorationManager,
            sessionFactory = sessionFactory,
            mediaBinder = mediaBinder,
            bookId = bookId
        )
    }
}
