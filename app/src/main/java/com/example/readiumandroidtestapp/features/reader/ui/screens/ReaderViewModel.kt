package com.example.readiumandroidtestapp.features.reader.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import com.example.readiumandroidtestapp.core.data.book.BookRepository
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import com.example.readiumandroidtestapp.features.reader.domain.OpenPublicationUseCase
import com.example.readiumandroidtestapp.features.reader.domain.ReaderDecorationManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderPreferencesManager
import com.example.readiumandroidtestapp.features.reader.domain.ReaderSessionFactory
import com.example.readiumandroidtestapp.features.reader.ui.audio.ReaderMediaBinder
import com.example.readiumandroidtestapp.features.reader.ui.search.ReaderSearchManager
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderError
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.Asset
import javax.inject.Inject
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
@HiltViewModel
class ReaderViewModel @Inject constructor(
    private val application: Application,
    private val bookRepository: BookRepository,
    private val openPublicationUseCase: OpenPublicationUseCase,
    private val searchManager: ReaderSearchManager,
    private val ttsManager: ReaderTtsManager,
    private val preferencesManager: ReaderPreferencesManager,
    private val decorationManager: ReaderDecorationManager,
    private val sessionFactory: ReaderSessionFactory,
    private val mediaBinder: ReaderMediaBinder,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReaderUiState>(value = ReaderUiState.Loading)
    val uiState: StateFlow<ReaderUiState> = _uiState.asStateFlow()

    private val _settingsSheetState = MutableStateFlow<ReaderSettingsSheet?>(value = null)
    val settingsSheetState: StateFlow<ReaderSettingsSheet?> = _settingsSheetState.asStateFlow()

    private val _bookId = MutableStateFlow<Long?>(value = null)

    // Reactive streams for book data, switched dynamically based on the current bookId.
    val bookmarks: Flow<List<Bookmark>> = _bookId.filterNotNull()
        .flatMapLatest { id -> bookRepository.bookmarksForBook(bookId = id) }

    val highlights: Flow<List<Highlight>> = _bookId.filterNotNull()
        .flatMapLatest { id -> bookRepository.highlightsForBook(bookId = id) }

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
    // to the VisualNavigator to control it from the ViewModel (e.g., for TTS page turning).
    private var currentVisualNavigator: VisualNavigator? = null
    private val visualLocatorFlow = MutableStateFlow<Locator?>(value = null)

    init {
        // Save progression (Debounced to avoid excessive database writes)
        visualLocatorFlow.debounce(timeout = 2.seconds).onEach { locator ->
            val id = _bookId.value
            if (locator != null && id != null) {
                val json = locator.toJSON().toString()
                bookRepository.saveProgression(bookId = id, locator = json)
            }
        }.launchIn(scope = viewModelScope)

        // Apply Search Decorations to the navigator whenever they change
        searchManager.searchDecorations.onEach { decorations ->
            (currentVisualNavigator as? DecorableNavigator)?.applyDecorations(
                decorations,
                group = "search",
            )
        }.launchIn(viewModelScope)
    }

    /**
     * Loads a book and initializes the appropriate session (Visual or Audio).
     */
    fun loadBook(id: Long) {
        if (_bookId.value == id && _uiState.value !is ReaderUiState.Error) return
        _bookId.value = id

        viewModelScope.launch {
            _uiState.value = ReaderUiState.Loading
            val book = bookRepository.get(id)
            val url = book?.url

            if (url == null) {
                _uiState.value = ReaderUiState.Error(error = ReaderError.InvalidBookLocation)
                return@launch
            }

            // Use Case handles file access and publication parsing
            openPublicationUseCase(url = url)
                .onSuccess { openedBook ->
                    asset = openedBook.asset
                    publication = openedBook.publication
                    setupSession(book = book, publication = openedBook.publication)
                }
                .onFailure { error ->
                    _uiState.value = ReaderUiState.Error(
                        error = ReaderError.PublicationOpenFailed(
                            cause = error as? Exception ?: Exception(error),
                        ),
                    )
                }
        }
    }

    private suspend fun setupSession(book: Book, publication: Publication) {
        if (publication.conformsTo(profile = Publication.Profile.AUDIOBOOK)) {
            sessionFactory.createAudioSession(book, publication).onSuccess { audioState ->
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
            val visualState = sessionFactory.createVisualSession(book, publication)
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

    //region Visual Navigator Interaction

    /**
     * Called when the VisualNavigator is ready (attached to the UI).
     *
     * This is critical for:
     * 1. Submitting initial preferences.
     * 2. Re-applying decorations (Highlights, Search).
     * 3. Initializing TTS with the publication.
     */
    fun onNavigatorReady(visualNavigator: VisualNavigator) {
        currentVisualNavigator = visualNavigator

        // Submit initial preferences to the navigator
        val state = _uiState.value as? ReaderUiState.Visual
        val preferences = state?.initialPreferences
        if (preferences != null) {
            if (preferences is EpubPreferences) {
                @Suppress("UNCHECKED_CAST")
                (visualNavigator as? Configurable<*, EpubPreferences>)?.submitPreferences(
                    preferences,
                )
            } else if (preferences is PdfiumPreferences) {
                @Suppress("UNCHECKED_CAST")
                (visualNavigator as? Configurable<*, PdfiumPreferences>)?.submitPreferences(
                    preferences,
                )
            }
        }

        // Restore Decorations (Highlights, Search)
        viewModelScope.launch {
            decorationManager.decorationFlow(bookId = _bookId.value).onEach { decorations ->
                (visualNavigator as? DecorableNavigator)?.applyDecorations(
                    decorations,
                    group = "highlights",
                )
            }.launchIn(scope = this)

            searchManager.searchDecorations.onEach { decorations ->
                (visualNavigator as? DecorableNavigator)?.applyDecorations(
                    decorations,
                    group = "search",
                )
            }.launchIn(scope = this)
        }

        // Initialize TTS factory
        publication?.let { ttsManager.initFactory(publication = it) }
    }

    fun onVisualLocatorChanged(locator: Locator) {
        visualLocatorFlow.value = locator
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
        _settingsSheetState.value = ReaderSettingsSheet.Configurable(editor = editor)
    }

    private fun openTtsSettings() {
        val pub = publication ?: return
        val id = _bookId.value ?: return

        viewModelScope.launch {
            val session = preferencesManager.createTtsSettingsSession(
                bookId = id,
                publication = pub,
                ttsManager = ttsManager,
                application = application,
            )
            if (session != null) {
                _settingsSheetState.value = ReaderSettingsSheet.Tts(session = session)
            }
        }
    }

    fun openAudiobookSettings() {
        val state = _uiState.value as? ReaderUiState.Audio ?: return
        val editor = state.preferencesEditor ?: return
        _settingsSheetState.value = ReaderSettingsSheet.Configurable(editor = editor)
    }

    fun closeSettings() {
        _settingsSheetState.value = null
    }

    fun onSettingsChanged(preferences: Configurable.Preferences<*>) {
        commitPreferences(preferences = preferences)
    }

    private fun commitPreferences(preferences: Configurable.Preferences<*>) {
        val id = _bookId.value ?: return
        viewModelScope.launch {
            preferencesManager.commitPreferences(
                bookId = id,
                preferences = preferences,
                currentVisualNavigator = currentVisualNavigator,
                audioNavigator = audioNavigator,
                ttsManager = ttsManager,
            )
            refreshSettings(preferences)
        }
    }

    private suspend fun refreshSettings(preferences: Configurable.Preferences<*>) {
        val sheetState = _settingsSheetState.value ?: return
        val currentState = _uiState.value

        when (sheetState) {
            is ReaderSettingsSheet.Configurable -> {
                val newState = preferencesManager.refreshSessionState(
                    currentState,
                    newPreferences = preferences,
                )

                if (newState != null) {
                    _uiState.value = newState

                    val newEditor = (newState as? ReaderUiState.Visual)?.preferencesEditor
                        ?: (newState as? ReaderUiState.Audio)?.preferencesEditor

                    if (newEditor != null) {
                        _settingsSheetState.value = ReaderSettingsSheet.Configurable(newEditor)
                    }
                }
            }

            is ReaderSettingsSheet.Tts -> {
                val visualState = currentState as? ReaderUiState.Visual ?: return
                val id = _bookId.value ?: return

                val newSession = preferencesManager.createTtsSettingsSession(
                    bookId = id,
                    publication = visualState.publication,
                    ttsManager = ttsManager,
                    application = application,
                )

                if (newSession != null) {
                    _settingsSheetState.value = ReaderSettingsSheet.Tts(newSession)
                }
            }
        }
    }
    //endregion

    //region Playback (TTS & Audio)
    fun startTts() {
        val navigator = currentVisualNavigator ?: return
        ttsManager.start(
            visualNavigator = navigator,
            scope = viewModelScope,
            onStop = ::stopTts,
        )
    }

    fun stopTts() = ttsManager.stop(currentVisualNavigator, viewModelScope)
    fun play() = ttsManager.play()
    fun pause() = ttsManager.pause()
    fun previous() = ttsManager.previous()
    fun next() = ttsManager.next()

    private fun startObservingAudioLocator(locatorFlow: StateFlow<Locator>) {
        locatorFlow.debounce(timeout = 2.seconds).onEach { locator ->
            val id = _bookId.value ?: return@onEach
            val json = locator.toJSON().toString()
            bookRepository.saveProgression(bookId = id, locator = json)
        }.launchIn(scope = viewModelScope)
    }
    //endregion

    //region Search & Highlights
    fun onSearchQueryChanged(query: String) = searchManager.onSearchQueryChanged(query = query)
    fun onHighlightAction(selection: Locator) = decorationManager.onHighlightAction(selection)
    fun dismissHighlightDialog() = decorationManager.dismissHighlightDialog()
    fun saveHighlight(note: String, color: Int) {
        val id = _bookId.value ?: return
        viewModelScope.launch {
            decorationManager.saveHighlight(bookId = id, note = note, color = color)
        }
    }
    //endregion
}
