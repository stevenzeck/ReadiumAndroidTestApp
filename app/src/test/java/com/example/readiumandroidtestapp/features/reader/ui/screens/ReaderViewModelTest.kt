package com.example.readiumandroidtestapp.features.reader.ui.screens

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.data.repository.FakeBookRepository
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.domain.OpenPublicationUseCase
import com.example.readiumandroidtestapp.features.reader.domain.OpenedBook
import com.example.readiumandroidtestapp.features.reader.domain.ReaderDecorationManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderPreferencesManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderSessionFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.ReaderMediaBinder
import com.example.readiumandroidtestapp.features.reader.ui.search.ReaderSearchManager
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettings
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
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.mediatype.MediaType

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ReaderViewModelTest {

    private lateinit var viewModel: ReaderViewModel
    private val bookRepository = FakeBookRepository()
    private val openPublicationUseCase: OpenPublicationUseCase = mockk()
    private val sessionFactory: ReaderSessionFactory = mockk()
    private val searchManager: ReaderSearchManager = mockk(relaxed = true)
    private val ttsManager: ReaderTtsManager = mockk(relaxed = true)
    private val preferencesManager: ReaderPreferencesManager = mockk(relaxed = true)
    private val decorationManager: ReaderDecorationManager = mockk(relaxed = true)
    private val mediaBinder: ReaderMediaBinder = mockk(relaxed = true)
    private val application: Application = mockk(relaxed = true)

    private val testDispatcher = StandardTestDispatcher()

    private val testBook = Book(
        id = 1L,
        href = "http://example.com/book.epub",
        title = "Test Book",
        identifier = "id",
        mediaType = MediaType.EPUB,
        cover = null,
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)
        every { ttsManager.isTtsActive } returns MutableStateFlow(value = false)
        every { decorationManager.showHighlightDialog } returns MutableStateFlow(value = false)

        runTest(context = testDispatcher) {
            bookRepository.addBooks(testBook)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initialization loads book and sets Visual state on success`() =
        runTest(context = testDispatcher) {
            // Arrange
            val publication = mockk<Publication>(relaxed = true)
            val openedBook = OpenedBook(
                publication = publication,
                asset = mockk<Asset>(relaxed = true),
            )
            val visualState = mockk<ReaderUiState.Visual>(relaxed = true)

            coEvery { openPublicationUseCase(url = any()) } returns Result.success(value = openedBook)
            coEvery {
                sessionFactory.createVisualSession(book = testBook, publication = publication)
            } returns visualState

            // Act
            viewModel = createViewModel(bookId = testBook.id)
            advanceUntilIdle()

            // Assert
            assertTrue(
                "State should be Visual",
                viewModel.uiState.value is ReaderUiState.Visual,
            )
        }

    @Test
    fun `initialization sets Error state when loading fails`() = runTest(context = testDispatcher) {
        // Arrange
        coEvery {
            openPublicationUseCase(url = any())
        } returns Result.failure(exception = Exception("Network Error"))

        // Act
        viewModel = createViewModel(bookId = testBook.id)
        advanceUntilIdle()

        // Assert
        assertTrue(
            "State should be Error",
            viewModel.uiState.value is ReaderUiState.Error,
        )
    }

    @Test
    fun `retryLoad attempts to fetch book data again`() = runTest(context = testDispatcher) {
        // Arrange - Fail first
        coEvery {
            openPublicationUseCase(url = any())
        } returns Result.failure(exception = Exception("Fail"))

        viewModel = createViewModel(bookId = testBook.id)
        advanceUntilIdle()
        assertTrue(
            "Initial state should be Error",
            viewModel.uiState.value is ReaderUiState.Error,
        )

        // Arrange - Prepare success for retry
        val publication = mockk<Publication>(relaxed = true)
        val openedBook = OpenedBook(
            publication = publication,
            asset = mockk(relaxed = true),
        )
        val visualState = mockk<ReaderUiState.Visual>(relaxed = true)

        coEvery { openPublicationUseCase(url = any()) } returns Result.success(value = openedBook)
        coEvery {
            sessionFactory.createVisualSession(book = testBook, publication = publication)
        } returns visualState

        // Act
        viewModel.retryLoad()
        advanceUntilIdle()

        // Assert
        assertTrue(
            "State should recover to Visual",
            viewModel.uiState.value is ReaderUiState.Visual,
        )
    }

    @Test
    fun `openSettings shows Configurable sheet when TTS is inactive`() =
        runTest(context = testDispatcher) {
            // Arrange
            val publication = mockk<Publication>(relaxed = true)
            coEvery {
                openPublicationUseCase(url = any())
            } returns Result.success(
                value = OpenedBook(publication = publication, asset = mockk(relaxed = true)),
            )

            // Use a visual state that returns a Configurable sheet when asked
            val visualState = mockk<ReaderUiState.Visual>(relaxed = true)
            coEvery {
                sessionFactory.createVisualSession(book = testBook, publication = publication)
            } returns visualState

            viewModel = createViewModel(bookId = testBook.id)
            advanceUntilIdle()

            // Act
            viewModel.openSettings()
            advanceUntilIdle()

            // Assert
            assertTrue(
                "Sheet should be Configurable",
                viewModel.settingsSheetState.value is ReaderSettings.Configurable,
            )
        }

    @Test
    fun `openSettings shows TTS sheet when TTS is active`() = runTest(context = testDispatcher) {
        // Arrange
        every { ttsManager.isTtsActive } returns MutableStateFlow(value = true)
        val publication = mockk<Publication>(relaxed = true)

        coEvery {
            openPublicationUseCase(url = any())
        } returns Result.success(
            value = OpenedBook(publication = publication, asset = mockk(relaxed = true)),
        )
        coEvery {
            sessionFactory.createVisualSession(book = testBook, publication = publication)
        } returns mockk<ReaderUiState.Visual>(relaxed = true)

        val mockSession = mockk<TtsSettingsSession>(relaxed = true)
        coEvery {
            preferencesManager.createTtsSettingsSession(
                bookId = any(),
                publication = any(),
                ttsManager = any(),
                application = any(),
            )
        } returns mockSession

        viewModel = createViewModel(bookId = testBook.id)
        advanceUntilIdle()

        // Act
        viewModel.openSettings()
        advanceUntilIdle()

        // Assert
        assertTrue(
            "Sheet should be Tts",
            viewModel.settingsSheetState.value is ReaderSettings.Tts,
        )
    }

    @Test
    fun `closeSettings clears the sheet state`() = runTest(context = testDispatcher) {
        // Arrange
        coEvery {
            openPublicationUseCase(url = any())
        } returns Result.success(
            value = OpenedBook(
                publication = mockk(relaxed = true),
                asset = mockk(relaxed = true),
            ),
        )
        coEvery {
            sessionFactory.createVisualSession(book = any(), publication = any())
        } returns mockk<ReaderUiState.Visual>(relaxed = true)

        viewModel = createViewModel(bookId = testBook.id)
        advanceUntilIdle()

        // Act
        viewModel.openAudiobookSettings()
        viewModel.closeSettings()
        advanceUntilIdle()

        // Assert
        assertNull(
            "Sheet state should be null",
            viewModel.settingsSheetState.value,
        )
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
