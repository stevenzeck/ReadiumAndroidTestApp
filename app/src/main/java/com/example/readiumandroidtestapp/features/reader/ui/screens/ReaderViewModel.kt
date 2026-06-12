package com.example.readiumandroidtestapp.features.reader.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.features.reader.domain.OpenPublicationUseCase
import com.example.readiumandroidtestapp.features.reader.domain.ReaderDecorationManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderPreferencesManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderSessionFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.ReaderMediaBinder
import com.example.readiumandroidtestapp.features.reader.ui.search.ReaderSearchManager
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderError
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderNavigator
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferences
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettings
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.navigator.common.DecorationController
import org.readium.navigator.common.NavigationController
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.Asset
import kotlin.time.Duration.Companion.seconds

/**
 * The ViewModel managing the Reader experience.
 *
 * Responsibilities:
 * 1. **Session Management**: Opening/closing publications and creating navigators via [ReaderSessionFactory].
 * 2. **State Orchestration**: Exposes the [ReaderUiState] (Loading, Error, Visual, Audio) to the UI.
 * 3. **Feature Delegation**: Delegates complex logic (Search, TTS, Preferences, Decorations) to specialized managers.
 * 4. **Persistence**: Saves reading progression, bookmarks, and highlights to the [BookRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel(assistedFactory = ReaderViewModel.Factory::class)
class ReaderViewModel @AssistedInject constructor(
    private val application: Application,
    private val bookRepository: BookRepository,
    private val openPublicationUseCase: OpenPublicationUseCase,
    private val searchManager: ReaderSearchManager,
    private val ttsManager: ReaderTtsManager,
    private val preferencesManager: ReaderPreferencesManager,
    private val decorationManager: ReaderDecorationManager,
    private val sessionFactory: ReaderSessionFactory,
    private val mediaBinder: ReaderMediaBinder,
    @Assisted val bookId: Long,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(value = ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val _settingsSheetState = MutableStateFlow<ReaderSettings?>(value = null)
    val settingsSheetState: StateFlow<ReaderSettings?> = _settingsSheetState.asStateFlow()

    // Reactive streams for book data, switched dynamically based on the current bookId.
    val bookmarks: Flow<List<Bookmark>> = bookRepository.bookmarksForBook(bookId = bookId)

    val highlights: Flow<List<Highlight>> = bookRepository.highlightsForBook(bookId = bookId)

    val showHighlightDialog: StateFlow<Boolean> = decorationManager.showHighlightDialog

    // Search and TTS state delegated to their respective managers.
    val searchQuery: StateFlow<String?> = searchManager.searchQuery
    val isTtsActive: StateFlow<Boolean> = ttsManager.isTtsActive
    val ttsPlayback: Flow<Boolean> = ttsManager.ttsPlayback

    val searchResults: Flow<PagingData<SearchItem>> = searchManager.getSearchResults(
        publicationFlow = _uiState.map { (it as? ReaderUiState.Visual)?.publication },
        scope = viewModelScope,
    )

    // Lifecycle-aware resources
    private var publication: Publication? = null
    private var asset: Asset? = null
    private var audioNavigator: AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>? = null

    // We hold a weak reference (not technically WeakReference, but nullable and transient)
    // to the VisualNavigator or NavigationController to control it from the ViewModel.
    private var currentVisualNavigator: VisualNavigator? = null
    private var currentNavigationController: NavigationController<*, *>? = null
    private val currentNavigator: ReaderNavigator?
        get() {
            currentVisualNavigator?.let { return ReaderNavigator.Legacy(it) }
            currentNavigationController?.let { return ReaderNavigator.New(it) }
            return null
        }
    private val visualLocatorFlow = MutableStateFlow<Locator?>(value = null)

    init {
        loadBookData()

        // Save progression (Debounced to avoid excessive database writes)
        visualLocatorFlow.debounce(timeout = 2.seconds).onEach { locator ->
            if (locator != null) {
                val json = locator.toJSON().toString()
                bookRepository.saveProgression(bookId = bookId, locator = json)
            }
        }.launchIn(scope = viewModelScope)

        // Apply Search Decorations to the legacy navigator (for PDFs)
        searchManager.pdfSearchDecorations.onEach { decorations ->
            (currentVisualNavigator as? DecorableNavigator)?.applyDecorations(
                decorations = decorations,
                group = "search",
            )
        }.launchIn(viewModelScope)
    }

    /**
     * Loads a book and initializes the appropriate session (Visual or Audio).
     */
    private fun loadBookData() {
        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading
            val book = bookRepository.get(bookId = bookId)
            val url = book?.url

            if (url == null) {
                _uiState.value = ReaderUiState.Error(error = ReaderError.InvalidBookLocation)
                return@launch
            }

            openPublicationUseCase(url = url).onSuccess { openedBook ->
                asset = openedBook.asset
                publication = openedBook.publication
                setupSession(book = book, publication = openedBook.publication)
            }.onFailure { error ->
                _uiState.value = ReaderUiState.Error(
                    error = ReaderError.PublicationOpenFailed(
                        cause = error as? Exception ?: Exception(error),
                    ),
                )
            }
        }
    }

    /**
     * Retries loading the book if an error occurred.
     */
    fun retryLoad() {
        if (_uiState.value !is ReaderUiState.Error) return
        loadBookData()
    }

    private suspend fun setupSession(book: Book, publication: Publication) {
        if (publication.conformsTo(profile = Publication.Profile.AUDIOBOOK)) {
            sessionFactory.createAudioSession(book = book, publication = publication)
                .onSuccess { audioState ->
                    audioNavigator = audioState.navigator
                    mediaBinder.bind(navigator = audioState.navigator)
                    startObservingAudioLocator(locatorFlow = audioState.navigator.currentLocator)
                    _uiState.value = audioState
                }.onFailure { error ->
                    _uiState.value = ReaderUiState.Error(
                        error = ReaderError.NavigatorCreationFailed(cause = error),
                    )
                }
        } else {
            val visualState =
                sessionFactory.createVisualSession(book = book, publication = publication)
            _uiState.value = visualState
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Cleanup all resources
        mediaBinder.unbind()
        audioNavigator?.close()
        ttsManager.close()
        publication?.close()
        asset?.close()
    }

    //region Navigator Interaction

    /**
     * Called when the Navigator is ready (attached to the UI).
     */
    fun onNavigatorReady(navigator: ReaderNavigator) {
        when (navigator) {
            is ReaderNavigator.Legacy -> {
                val visualNavigator = navigator.navigator
                currentVisualNavigator = visualNavigator
                currentNavigationController = null

                // Submit initial preferences to the navigator
                val state = _uiState.value as? ReaderUiState.Visual
                val preferences = state?.initialPreferences
                if (preferences != null) {
                    if (preferences is ReaderPreferences.Pdf) {
                        @Suppress("UNCHECKED_CAST") (visualNavigator as? Configurable<*, PdfiumPreferences>)?.submitPreferences(
                            preferences = preferences.value,
                        )
                    }
                }

                // Restore Decorations (Highlights, Search) for PDF
                viewModelScope.launch {
                    decorationManager.pdfDecorationFlow(bookId = bookId).onEach { decorations ->
                        (visualNavigator as? DecorableNavigator)?.applyDecorations(
                            decorations = decorations,
                            group = "highlights",
                        )
                    }.launchIn(scope = this)

                    searchManager.pdfSearchDecorations.onEach { decorations ->
                        (visualNavigator as? DecorableNavigator)?.applyDecorations(
                            decorations = decorations,
                            group = "search",
                        )
                    }.launchIn(scope = this)
                }
            }

            is ReaderNavigator.New -> {
                val controller = navigator.controller
                currentNavigationController = controller
                currentVisualNavigator = null

                // Restore Decorations (Highlights, Search) for EPUB
                viewModelScope.launch {
                    val isFixedLayout =
                        (uiState.value as? ReaderUiState.Visual)?.isFixedLayout ?: false

                    decorationManager.epubDecorationFlow(
                        bookId = bookId,
                        isFixedLayout = isFixedLayout,
                    )
                        .onEach { decorations ->
                            @Suppress("UNCHECKED_CAST")
                            (controller as? DecorationController<org.readium.navigator.common.DecorationLocation>)?.let { decController ->
                                decController.decorations =
                                    decController.decorations.put(
                                        "highlights",
                                        decorations.toPersistentList(),
                                    )
                            }
                        }.launchIn(scope = this)

                    searchManager.epubSearchDecorations(isFixedLayout = isFixedLayout)
                        .onEach { decorations ->
                            @Suppress("UNCHECKED_CAST")
                            (controller as? DecorationController<org.readium.navigator.common.DecorationLocation>)?.let { decController ->
                                decController.decorations =
                                    decController.decorations.put(
                                        "search",
                                        decorations.toPersistentList(),
                                    )
                            }
                        }.launchIn(scope = this)
                }
            }
        }

        // Initialize TTS factory
        publication?.let { ttsManager.initFactory(publication = it) }
    }

    fun onVisualLocatorChanged(locator: Locator) {
        visualLocatorFlow.value = locator
        val currentState = _uiState.value
        if (currentState is ReaderUiState.Visual) {
            _uiState.value = currentState.copy(initialLocator = locator)
        }
    }
    //endregion

    //region Settings Management
    fun openSettings() {
        if (ttsManager.isTtsActive.value) {
            openTtsSettings()
        } else {
            openVisualSettings()
        }
    }

    private fun openVisualSettings() {
        val state = _uiState.value as? ReaderUiState.Visual ?: return
        val editor = state.preferencesEditor ?: return
        _settingsSheetState.value = ReaderSettings.Configurable(editor = editor)
    }

    private fun openTtsSettings() {
        val pub = publication ?: return

        viewModelScope.launch {
            val session = preferencesManager.createTtsSettingsSession(
                bookId = bookId,
                publication = pub,
                ttsManager = ttsManager,
                application = application,
            )
            if (session != null) {
                _settingsSheetState.value = ReaderSettings.Tts(session = session)
            }
        }
    }

    fun openAudiobookSettings() {
        val state = _uiState.value as? ReaderUiState.Audio ?: return
        val editor = state.preferencesEditor ?: return

        _settingsSheetState.value = ReaderSettings.Configurable(editor = editor)
    }

    fun closeSettings() {
        _settingsSheetState.value = null
    }

    fun onSettingsChanged(preferences: ReaderPreferences) {
        commitPreferences(preferences = preferences)
    }

    private fun commitPreferences(preferences: ReaderPreferences) {
        val pub = publication ?: return
        viewModelScope.launch {
            val navigator = currentVisualNavigator?.let { ReaderNavigator.Legacy(navigator = it) }
                ?: currentNavigationController?.let { ReaderNavigator.New(controller = it) }

            preferencesManager.commitPreferences(
                bookId = bookId,
                preferences = preferences,
                publication = pub,
                navigator = navigator,
                audioNavigator = audioNavigator,
                ttsManager = ttsManager,
            )
            refreshSettings(preferences = preferences)
        }
    }

    private suspend fun refreshSettings(preferences: ReaderPreferences) {
        val sheetState = _settingsSheetState.value ?: return
        val currentState = _uiState.value

        when (sheetState) {
            is ReaderSettings.Configurable -> {
                val newState = preferencesManager.refreshSessionState(
                    currentState = currentState,
                    newPreferences = preferences,
                )

                if (newState != null) {
                    _uiState.value = newState

                    val newEditor = (newState as? ReaderUiState.Visual)?.preferencesEditor
                        ?: (newState as? ReaderUiState.Audio)?.preferencesEditor

                    if (newEditor != null) {
                        _settingsSheetState.value = ReaderSettings.Configurable(editor = newEditor)
                    }
                }
            }

            is ReaderSettings.Tts -> {
                val visualState = currentState as? ReaderUiState.Visual ?: return

                val newSession = preferencesManager.createTtsSettingsSession(
                    bookId = bookId,
                    publication = visualState.publication,
                    ttsManager = ttsManager,
                    application = application,
                )

                if (newSession != null) {
                    _settingsSheetState.value = ReaderSettings.Tts(session = newSession)
                }
            }
        }
    }
    //endregion

    //region Playback (TTS & Audio)
    fun startTts() {
        val navigator = currentNavigator ?: return
        ttsManager.start(
            navigator = navigator,
            scope = viewModelScope,
            onStop = ::stopTts,
        )
    }

    fun stopTts() = ttsManager.stop(currentNavigator, viewModelScope)
    fun play() = ttsManager.play()
    fun pause() = ttsManager.pause()
    fun previous() = ttsManager.previous()
    fun next() = ttsManager.next()

    private fun startObservingAudioLocator(locatorFlow: StateFlow<Locator>) {
        locatorFlow.debounce(timeout = 2.seconds).onEach { locator ->
            val json = locator.toJSON().toString()
            bookRepository.saveProgression(bookId = bookId, locator = json)
        }.launchIn(scope = viewModelScope)
    }
    //endregion

    //region Search & Highlights
    fun onSearchQueryChanged(query: String) = searchManager.onSearchQueryChanged(query = query)
    fun onHighlightAction(selection: Locator) =
        decorationManager.onHighlightAction(selection = selection)
    fun dismissHighlightDialog() = decorationManager.dismissHighlightDialog()
    fun saveHighlight(note: String, color: Int) {
        viewModelScope.launch {
            decorationManager.saveHighlight(bookId = bookId, note = note, color = color)
        }
    }
    //endregion

    @AssistedFactory
    interface Factory {
        fun create(bookId: Long): ReaderViewModel
    }
}
