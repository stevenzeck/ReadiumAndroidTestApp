package com.example.readiumandroidtestapp.features.reader.ui.screens

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
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
import org.junit.runner.RunWith
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.mediatype.MediaType

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
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

    private val testBookId = 1L
    private val testBookUrl = AbsoluteUrl(url = "http://example.com/book.epub")!!
    private val testBook = Book(
        id = testBookId,
        href = testBookUrl.toString(),
        title = "Test Book",
        identifier = "id",
        mediaType = MediaType(string = "application/epub+zip")!!,
        cover = null,
    )

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
        prepareSuccessfulLoad(book = testBook, isVisual = true)

        viewModel = createViewModel(bookId = testBookId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Visual)
    }

    @Test
    fun `initialization sets Error state when loading fails`() = runTest(testDispatcher) {
        coEvery { bookRepository.get(bookId = testBookId) } returns testBook
        coEvery { openPublicationUseCase(url = any()) } returns Result.failure(Exception("Network Error"))

        viewModel = createViewModel(bookId = testBookId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Error)
    }

    @Test
    fun `retryLoad attempts to fetch book data again`() = runTest(testDispatcher) {
        // Initial failure
        coEvery { bookRepository.get(bookId = testBookId) } returns testBook
        coEvery { openPublicationUseCase(url = any()) } returns Result.failure(Exception("Fail"))

        viewModel = createViewModel(bookId = testBookId)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is ReaderUiState.Error)

        // Reset and prepare success for retry
        prepareSuccessfulLoad(book = testBook, isVisual = true)

        viewModel.retryLoad()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Visual)
    }

    @Test
    fun `openSettings shows Configurable sheet when TTS is inactive`() = runTest(testDispatcher) {
        every { ttsManager.isTtsActive } returns MutableStateFlow(false)

        val mockEditor = mockk<PreferencesEditor<*>>(relaxed = true)

        prepareSuccessfulLoad(
            book = testBook,
            isVisual = true,
            preferencesEditor = mockEditor,
        )

        viewModel = createViewModel(bookId = testBookId)
        advanceUntilIdle()

        viewModel.openSettings()
        advanceUntilIdle()

        assertTrue(viewModel.settingsSheetState.value is ReaderSettingsSheet.Configurable)
    }

    @Test
    fun `openSettings shows TTS sheet when TTS is active`() = runTest(testDispatcher) {
        every { ttsManager.isTtsActive } returns MutableStateFlow(true)
        prepareSuccessfulLoad(book = testBook, isVisual = true)

        val mockSession = mockk<TtsSettingsSession>(relaxed = true)
        coEvery {
            preferencesManager.createTtsSettingsSession(
                bookId = any(),
                publication = any(),
                ttsManager = any(),
                application = any(),
            )
        } returns mockSession

        viewModel = createViewModel(bookId = testBookId)
        advanceUntilIdle()

        viewModel.openSettings()
        advanceUntilIdle()

        assertTrue(viewModel.settingsSheetState.value is ReaderSettingsSheet.Tts)
    }

    @Test
    fun `closeSettings clears the sheet state`() = runTest(testDispatcher) {
        prepareSuccessfulLoad(book = testBook, isVisual = true)

        viewModel = createViewModel(bookId = testBookId)
        advanceUntilIdle()

        viewModel.openAudiobookSettings()
        viewModel.closeSettings()
        advanceUntilIdle()

        assertNull(viewModel.settingsSheetState.value)
    }

    // --- Helper Methods ---

    private fun prepareSuccessfulLoad(
        book: Book,
        publication: Publication = mockk(relaxed = true),
        isVisual: Boolean,
        preferencesEditor: PreferencesEditor<*>? = null,
    ) {
        coEvery { bookRepository.get(bookId = book.id) } returns book

        val openedBook = OpenedBook(publication = publication, asset = mockk(relaxed = true))
        coEvery { openPublicationUseCase(url = book.url!!) } returns Result.success(value = openedBook)

        if (isVisual) {
            val visualState = createVisualState(
                publication = publication,
                book = book,
                preferencesEditor = preferencesEditor,
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

    private fun createVisualState(
        publication: Publication = mockk(relaxed = true),
        book: Book = mockk(relaxed = true),
        preferencesEditor: PreferencesEditor<*>? = null,
    ) = ReaderUiState.Visual(
        publication = publication,
        book = book,
        initialLocator = null,
        pdfiumDocumentFactory = mockk(),
        capabilities = mockk(),
        preferencesEditor = preferencesEditor,
        initialPreferences = mockk(),
        isFixedLayout = false,
    )
}
