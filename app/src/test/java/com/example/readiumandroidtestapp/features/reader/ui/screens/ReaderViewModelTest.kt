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
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.epub.EpubSettings
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset

@OptIn(ExperimentalCoroutinesApi::class)
class ReaderViewModelTest {

    private lateinit var viewModel: ReaderViewModel
    private val application: Application = mockk()
    private val bookRepository: BookRepository = mockk(relaxed = true)
    private val openPublicationUseCase: OpenPublicationUseCase = mockk()
    private val searchManager: ReaderSearchManager = mockk(relaxed = true)
    private val ttsManager: ReaderTtsManager = mockk(relaxed = true)
    private val preferencesManager: ReaderPreferencesManager = mockk(relaxed = true)
    private val decorationManager: ReaderDecorationManager = mockk(relaxed = true)
    private val sessionFactory: ReaderSessionFactory = mockk(relaxed = true)
    private val mediaBinder: ReaderMediaBinder = mockk(relaxed = true)
    private val testDispatcher = StandardTestDispatcher()

    interface TestVisualNavigator : VisualNavigator, Configurable<EpubSettings, EpubPreferences>,
        DecorableNavigator

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher = testDispatcher)

        every { bookRepository.bookmarksForBook(bookId = any()) } returns emptyFlow()
        every { bookRepository.highlightsForBook(bookId = any()) } returns emptyFlow()
        every { searchManager.searchQuery } returns MutableStateFlow(value = null)
        every { ttsManager.isTtsActive } returns MutableStateFlow(value = false)
        every { ttsManager.ttsPlayback } returns emptyFlow()
        every {
            searchManager.getSearchResults(
                publicationFlow = any(),
                scope = any(),
            )
        } returns emptyFlow()
        every { searchManager.searchDecorations } returns emptyFlow()
        every { decorationManager.showHighlightDialog } returns MutableStateFlow(value = false)
        every { decorationManager.decorationFlow(bookId = any()) } returns emptyFlow()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadBookData success opens publication`() = runTest(context = testDispatcher) {
        val bookId = 1L
        val bookUrl = mockk<AbsoluteUrl>()
        val book = mockk<Book>(relaxed = true) {
            every { url } returns bookUrl
        }
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = any()) } returns false // Visual
        }
        val asset = mockk<Asset>(relaxed = true)
        val openedBook = OpenedBook(publication = publication, asset = asset)

        coEvery { bookRepository.get(bookId) } returns book
        coEvery { openPublicationUseCase(bookUrl) } returns Result.success(value = openedBook)
        coEvery {
            sessionFactory.createVisualSession(
                book,
                publication,
            )
        } returns ReaderUiState.Visual(
            publication = publication,
            book = book,
            initialLocator = null,
            pdfiumDocumentFactory = mockk(),
            capabilities = mockk(),
            preferencesEditor = null,
            initialPreferences = mockk(),
            isFixedLayout = false,
        )

        viewModel = createViewModel(bookId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Visual)
    }

    @Test
    fun `loadBookData failure emits Error`() = runTest(context = testDispatcher) {
        val bookId = 1L
        val bookUrl = mockk<AbsoluteUrl>()
        val book = mockk<Book>(relaxed = true) {
            every { url } returns bookUrl
        }

        coEvery { bookRepository.get(bookId) } returns book
        coEvery { openPublicationUseCase(bookUrl) } returns Result.failure(Exception("Failed"))

        viewModel = createViewModel(bookId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Error)
    }

    @Test
    fun `retryLoad re-calls loadBookData`() = runTest(context = testDispatcher) {
        val bookId = 1L
        val bookUrl = mockk<AbsoluteUrl>()
        val book = mockk<Book>(relaxed = true) {
            every { url } returns bookUrl
        }
        coEvery { bookRepository.get(bookId = bookId) } returns book
        coEvery { openPublicationUseCase(url = bookUrl) } returns Result.failure(Exception("Fail"))

        viewModel = createViewModel(bookId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Error)

        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = any()) } returns false
        }
        val openedBook = OpenedBook(publication = publication, asset = mockk(relaxed = true))
        coEvery { openPublicationUseCase(bookUrl) } returns Result.success(value = openedBook)
        coEvery {
            sessionFactory.createVisualSession(book = book, publication = publication)
        } returns ReaderUiState.Visual(
            publication = publication,
            book = book,
            initialLocator = null,
            pdfiumDocumentFactory = mockk(),
            capabilities = mockk(),
            preferencesEditor = null,
            initialPreferences = mockk(),
            isFixedLayout = false,
        )

        viewModel.retryLoad()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Visual)
    }

    @Test
    fun `openSettings delegates to ttsManager when active`() = runTest(context = testDispatcher) {
        val bookId = 1L
        every { ttsManager.isTtsActive } returns MutableStateFlow(value = true)
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = any()) } returns false
        }
        val book = mockk<Book>(relaxed = true)

        setupVisualSession(bookId = bookId, publication = publication, book = book)

        coEvery {
            preferencesManager.createTtsSettingsSession(
                bookId = bookId,
                publication = publication,
                ttsManager = ttsManager,
                application = application,
            )
        } returns mockk(relaxed = true)

        viewModel.openSettings()
        advanceUntilIdle()

        coVerify {
            preferencesManager.createTtsSettingsSession(
                bookId = bookId,
                publication = publication,
                ttsManager = ttsManager,
                application = application,
            )
        }
    }

    @Test
    fun `openSettings opens visual settings when TTS inactive`() =
        runTest(context = testDispatcher) {
            val bookId = 1L
            every { ttsManager.isTtsActive } returns MutableStateFlow(value = false)
            val publication = mockk<Publication>(relaxed = true) {
                every { conformsTo(profile = any()) } returns false
            }
            val book = mockk<Book>(relaxed = true)
            val editor = mockk<PreferencesEditor<*>>()

            setupVisualSession(
                bookId = bookId,
                publication = publication,
                book = book,
                editor = editor,
            )

            viewModel.openSettings()
            advanceUntilIdle()

            assertTrue(viewModel.settingsSheetState.value is ReaderSettingsSheet.Configurable)
        }

    @Test
    fun `startTts delegates to ttsManager`() = runTest(context = testDispatcher) {
        val bookId = 1L
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = any()) } returns false
        }
        val book = mockk<Book>(relaxed = true)

        setupVisualSession(bookId, publication, book)
        val navigator = mockk<VisualNavigator>(relaxed = true)
        viewModel.onNavigatorReady(visualNavigator = navigator)

        viewModel.startTts()
        advanceUntilIdle()

        coVerify { ttsManager.start(visualNavigator = navigator, scope = any(), onStop = any()) }
    }

    @Test
    fun `stopTts delegates to ttsManager`() = runTest(context = testDispatcher) {
        val bookId = 1L
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = any()) } returns false
        }
        val book = mockk<Book>(relaxed = true)

        setupVisualSession(bookId, publication, book)
        val navigator = mockk<VisualNavigator>(relaxed = true)
        viewModel.onNavigatorReady(visualNavigator = navigator)

        viewModel.stopTts()
        advanceUntilIdle()

        coVerify { ttsManager.stop(visualNavigator = navigator, scope = any()) }
    }

    @Test
    fun `onNavigatorReady submits initial preferences and applies decorations`() =
        runTest(context = testDispatcher) {
            val bookId = 1L
            val publication = mockk<Publication>(relaxed = true)
            val book = mockk<Book>(relaxed = true) {
                every { url } returns mockk()
            }
            val preferences = EpubPreferences()

            val openedBook = OpenedBook(publication = publication, asset = mockk(relaxed = true))
            coEvery { bookRepository.get(bookId = bookId) } returns book
            coEvery { openPublicationUseCase(url = any()) } returns Result.success(value = openedBook)
            coEvery {
                sessionFactory.createVisualSession(book, publication)
            } returns ReaderUiState.Visual(
                publication = publication,
                book = book,
                initialLocator = null,
                pdfiumDocumentFactory = mockk(),
                capabilities = mockk(),
                preferencesEditor = null,
                initialPreferences = preferences,
                isFixedLayout = false,
            )

            viewModel = createViewModel(bookId)
            advanceUntilIdle()

            val navigator = mockk<TestVisualNavigator>(relaxed = true)
            viewModel.onNavigatorReady(visualNavigator = navigator)
            advanceUntilIdle()

            verify { navigator.submitPreferences(preferences = preferences) }
        }

    @Test
    fun `commitPreferences delegates to manager and refreshes session`() =
        runTest(context = testDispatcher) {
            val bookId = 1L
            val publication = mockk<Publication>(relaxed = true)
            val book = mockk<Book>(relaxed = true)
            val editor = mockk<PreferencesEditor<*>>(relaxed = true)

            setupVisualSession(bookId, publication, book, editor)

            viewModel.openSettings()
            advanceUntilIdle()

            val newPreferences = mockk<Configurable.Preferences<*>>()
            val newState = mockk<ReaderUiState.Visual>()
            every { newState.preferencesEditor } returns editor
            every {
                preferencesManager.refreshSessionState(
                    currentState = any(),
                    newPreferences = newPreferences,
                )
            } returns newState

            viewModel.onSettingsChanged(preferences = newPreferences)
            advanceUntilIdle()

            coVerify {
                preferencesManager.commitPreferences(
                    bookId = bookId,
                    preferences = newPreferences,
                    currentVisualNavigator = any(),
                    audioNavigator = any(),
                    ttsManager = ttsManager,
                )
            }
            verify {
                preferencesManager.refreshSessionState(
                    currentState = any(),
                    newPreferences = newPreferences,
                )
            }
        }

    @Test
    fun `onVisualLocatorChanged saves progression`() = runTest(context = testDispatcher) {
        val bookId = 1L
        val publication = mockk<Publication>(relaxed = true)
        val book = mockk<Book>(relaxed = true)

        setupVisualSession(bookId, publication, book)

        val locator = mockk<Locator>(relaxed = true)
        every { locator.toJSON().toString() } returns "{}"

        viewModel.onVisualLocatorChanged(locator = locator)

        advanceTimeBy(delayTimeMillis = 3000)
        advanceUntilIdle()

        coVerify {
            bookRepository.saveProgression(bookId = bookId, locator = "{}")
        }
    }

    @Test
    fun `loadBookData success opens audio publication`() = runTest(context = testDispatcher) {
        val bookId = 1L
        val bookUrl = mockk<AbsoluteUrl>()
        val book = mockk<Book>(relaxed = true) {
            every { url } returns bookUrl
        }
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = Publication.Profile.AUDIOBOOK) } returns true
        }
        val openedBook = OpenedBook(publication = publication, asset = mockk(relaxed = true))

        coEvery { bookRepository.get(bookId = bookId) } returns book
        coEvery { openPublicationUseCase(url = bookUrl) } returns Result.success(value = openedBook)

        val audioState = ReaderUiState.Audio(
            publication = publication,
            book = book,
            navigator = mockk(relaxed = true),
            preferencesEditor = mockk(relaxed = true),
        )

        coEvery {
            sessionFactory.createAudioSession(book = book, publication = publication)
        } returns Result.success(value = audioState)

        viewModel = createViewModel(bookId = bookId)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is ReaderUiState.Audio)
        verify { mediaBinder.bind(navigator = any()) }
    }

    @Test
    fun `openAudiobookSettings opens settings sheet`() = runTest(context = testDispatcher) {
        val bookId = 1L
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = Publication.Profile.AUDIOBOOK) } returns true
        }
        val book = mockk<Book>(relaxed = true)
        val editor = mockk<PreferencesEditor<ExoPlayerPreferences>>(relaxed = true)

        val audioState = ReaderUiState.Audio(
            publication = publication,
            book = book,
            navigator = mockk(relaxed = true),
            preferencesEditor = editor,
        )

        val bookUrl = mockk<AbsoluteUrl>()
        every { book.url } returns bookUrl
        val openedBook = OpenedBook(publication = publication, asset = mockk(relaxed = true))
        coEvery { bookRepository.get(bookId = bookId) } returns book
        coEvery { openPublicationUseCase(url = bookUrl) } returns Result.success(value = openedBook)
        coEvery { sessionFactory.createAudioSession(book = book, publication = publication) } returns Result.success(audioState)

        viewModel = createViewModel(bookId = bookId)
        advanceUntilIdle()

        viewModel.openAudiobookSettings()
        advanceUntilIdle()

        assertTrue(viewModel.settingsSheetState.value is ReaderSettingsSheet.Configurable)
    }

    @Test
    fun `playback controls delegate to ttsManager`() = runTest(context = testDispatcher) {
        val bookId = 1L
        coEvery { bookRepository.get(bookId = bookId) } returns mockk(relaxed = true)
        coEvery { openPublicationUseCase(url = any()) } returns Result.failure(Exception("Skip load"))

        viewModel = createViewModel(bookId = bookId)

        viewModel.play()
        verify { ttsManager.play() }

        viewModel.pause()
        verify { ttsManager.pause() }

        viewModel.previous()
        verify { ttsManager.previous() }

        viewModel.next()
        verify { ttsManager.next() }
    }

    @Test
    fun `closeSettings clears sheet state`() = runTest(context = testDispatcher) {
        val bookId = 1L
        coEvery { bookRepository.get(bookId = bookId) } returns mockk(relaxed = true)
        coEvery { openPublicationUseCase(url = any()) } returns Result.failure(Exception("Skip load"))

        viewModel = createViewModel(bookId = bookId)

        viewModel.closeSettings()
        advanceUntilIdle()

        assertTrue(viewModel.settingsSheetState.value == null)
    }

    private fun TestScope.setupVisualSession(
        bookId: Long,
        publication: Publication,
        book: Book,
        editor: PreferencesEditor<*>? = null,
    ) {
        val bookUrl = mockk<AbsoluteUrl>()
        every { book.url } returns bookUrl

        val openedBook = OpenedBook(publication = publication, asset = mockk(relaxed = true))
        coEvery { bookRepository.get(bookId = bookId) } returns book
        coEvery { openPublicationUseCase(url = bookUrl) } returns Result.success(value = openedBook)
        coEvery {
            sessionFactory.createVisualSession(
                book,
                publication,
            )
        } returns ReaderUiState.Visual(
            publication = publication,
            book = book,
            initialLocator = null,
            pdfiumDocumentFactory = mockk(),
            capabilities = mockk(),
            preferencesEditor = editor,
            initialPreferences = mockk(),
            isFixedLayout = false,
        )

        viewModel = createViewModel(bookId = bookId)
        advanceUntilIdle()
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
