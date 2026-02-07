package com.example.readiumandroidtestapp.features.reader.ui.screens

import android.app.Application
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.features.reader.domain.OpenPublicationUseCase
import com.example.readiumandroidtestapp.features.reader.domain.OpenedBook
import com.example.readiumandroidtestapp.features.reader.domain.ReaderDecorationManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderPreferencesManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderSessionFactory
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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.readium.r2.navigator.preferences.PreferencesEditor
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

    @Test
    fun `initialization loads book and sets Visual state on success`() = runTest(testDispatcher) {
        val bookId = 1L
        prepareSuccessfulLoad(bookId = bookId, isVisual = true)

        viewModel = createViewModel(bookId = bookId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Visual)
    }

    @Test
    fun `initialization sets Error state when loading fails`() = runTest(testDispatcher) {
        val bookId = 1L
        val book = mockk<Book>(relaxed = true)

        coEvery { bookRepository.get(bookId = bookId) } returns book
        coEvery { openPublicationUseCase(url = any()) } returns Result.failure(Exception("Network Error"))

        viewModel = createViewModel(bookId = bookId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Error)
    }

    @Test
    fun `retryLoad attempts to fetch book data again`() = runTest(testDispatcher) {
        val bookId = 1L
        val book = mockk<Book>(relaxed = true)
        coEvery { bookRepository.get(bookId = bookId) } returns book
        coEvery { openPublicationUseCase(url = any()) } returns Result.failure(Exception("Fail"))

        viewModel = createViewModel(bookId = bookId)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ReaderUiState.Error)

        // Reset and prepare success for retry
        prepareSuccessfulLoad(bookId = bookId, book = book, isVisual = true)
        viewModel.retryLoad()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Visual)
    }

    @Test
    fun `openSettings shows Configurable sheet when TTS is inactive`() = runTest(testDispatcher) {
        // GIVEN a visual session with TTS off AND a valid PreferencesEditor
        every { ttsManager.isTtsActive } returns MutableStateFlow(false)

        // Fix: We must provide a mock editor, otherwise openSettings ignores the call
        val mockEditor = mockk<PreferencesEditor<*>>(relaxed = true)

        prepareSuccessfulLoad(
            bookId = 1L,
            isVisual = true,
            preferencesEditor = mockEditor,
        )

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
        every { ttsManager.isTtsActive } returns MutableStateFlow(true)
        prepareSuccessfulLoad(bookId = 1L, isVisual = true)

        val mockSession = mockk<TtsSettingsSession>(relaxed = true)
        coEvery {
            preferencesManager.createTtsSettingsSession(
                bookId = any(),
                publication = any(),
                ttsManager = any(),
                application = any(),
            )
        } returns mockSession

        viewModel = createViewModel(bookId = 1L)
        advanceUntilIdle()

        viewModel.openSettings()
        advanceUntilIdle()

        assertTrue(viewModel.settingsSheetState.value is ReaderSettingsSheet.Tts)
    }

    @Test
    fun `closeSettings clears the sheet state`() = runTest(testDispatcher) {
        prepareSuccessfulLoad(bookId = 1L, isVisual = true)

        viewModel = createViewModel(bookId = 1L)
        advanceUntilIdle()

        // Open a sheet manually or via method
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
        book: Book = mockk(relaxed = true),
        publication: Publication = mockk(relaxed = true),
        isVisual: Boolean,
        preferencesEditor: PreferencesEditor<*>? = null,
    ) {
        val bookUrl = mockk<AbsoluteUrl>()
        every { book.url } returns bookUrl

        coEvery { bookRepository.get(bookId = bookId) } returns book

        val openedBook = OpenedBook(publication = publication, asset = mockk(relaxed = true))
        coEvery { openPublicationUseCase(url = bookUrl) } returns Result.success(value = openedBook)

        if (isVisual) {
            val visualState = ReaderUiState.Visual(
                publication = publication,
                book = book,
                initialLocator = null,
                pdfiumDocumentFactory = mockk(),
                capabilities = mockk(),
                preferencesEditor = preferencesEditor,
                initialPreferences = mockk(),
                isFixedLayout = false,
            )
            coEvery {
                sessionFactory.createVisualSession(
                    book = book,
                    publication = publication,
                )
            } returns visualState
        } else {
            val audioState = ReaderUiState.Audio(
                publication = publication,
                book = book,
                navigator = mockk(relaxed = true),
                preferencesEditor = mockk(relaxed = true),
            )
            coEvery {
                sessionFactory.createAudioSession(
                    book = book,
                    publication = publication,
                )
            } returns Result.success(value = audioState)
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
            bookId = bookId,
        )
    }
}
