package com.example.readiumandroidtestapp.features.reader.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LoadingIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import com.example.readiumandroidtestapp.features.reader.ui.audio.AudioReader
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import com.example.readiumandroidtestapp.features.reader.ui.visual.VisualReaderContent
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.publication.Locator

data class VisualReaderActions(
    val onSettingsClick: () -> Unit,
    val onSettingsChange: (Configurable.Preferences<*>) -> Unit,
    val onSettingsDismiss: () -> Unit,
    val onVisualLocatorChanged: (Locator) -> Unit,
    val onNavigatorReady: (VisualNavigator) -> Unit,
    val onAnnotateAction: (Locator) -> Unit,
    val startTts: () -> Unit,
    val stopTts: () -> Unit,
    val play: () -> Unit,
    val pause: () -> Unit,
    val previous: () -> Unit,
    val next: () -> Unit,
    val onSearchQueryChanged: (String) -> Unit,
    val saveAnnotation: (String, Int, ReaderAnnotation.Style) -> Unit,
    val dismissAnnotationDialog: () -> Unit,
    val onDeleteAnnotation: (ReaderAnnotation) -> Unit,
    val onEditAnnotationAction: (ReaderAnnotation) -> Unit,
    val onNavigateBack: () -> Unit,
)

data class VisualReaderState(
    val uiState: ReaderUiState.Visual,
    val settingsSheetState: ReaderSettingsSheet?,
    val bookmarks: List<Bookmark>,
    val annotations: List<ReaderAnnotation>,
    val searchResults: LazyPagingItems<SearchItem>,
    val searchQuery: String?,
    val isTtsActive: Boolean,
    val isPlaying: Boolean,
    val showAnnotationDialog: Boolean,
    val editingAnnotation: ReaderAnnotation?,
)

data class AudioReaderActions(
    val onSettingsClick: () -> Unit,
    val onSettingsChange: (Configurable.Preferences<*>) -> Unit,
    val onSettingsDismiss: () -> Unit,
    val onNavigateBack: () -> Unit,
)

data class AudioReaderState(
    val uiState: ReaderUiState.Audio,
    val settingsSheetState: ReaderSettingsSheet?,
)

/**
 * The main UI composable for the Reader feature.
 *
 * This screen acts as the **state host** for the reading experience. It:
 * 1. Collects state from the [ReaderViewModel].
 * 2. Delegates rendering to format-specific composables ([VisualReaderContent] or [AudioReader]).
 * 3. Hoists navigation and lifecycle events up to the ViewModel or Navigator.
 *
 * @param bookId The ID of the book to display. Used to trigger the initial load.
 * @param viewModel The view model that manages the Readium session and app state.
 * @param onNavigateBack Callback to handle the "Up" or "Back" action.
 */
@Composable
fun ReaderScreen(
    bookId: Long,
    viewModel: ReaderViewModel = hiltViewModel(
        creationCallback = { factory: ReaderViewModel.Factory ->
            factory.create(bookId)
        },
    ),
    onNavigateBack: () -> Unit,
    audioReaderContent: @Composable (AudioReaderState, AudioReaderActions) -> Unit = { state, actions ->
        DefaultAudioReaderContent(state = state, actions = actions)
    },
    visualReaderContent: @Composable (VisualReaderState, VisualReaderActions) -> Unit = { state, actions ->
        DefaultVisualReaderContent(state = state, actions = actions)
    },
) {
    val state by viewModel.uiState.collectAsState()
    val settingsSheetState by viewModel.settingsSheetState.collectAsState()

    val bookmarks by viewModel.bookmarks.collectAsState(initial = emptyList())
    val annotations by viewModel.annotations.collectAsState(initial = emptyList())
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isTtsActive by viewModel.isTtsActive.collectAsState()
    val isPlaying by viewModel.ttsPlayback.collectAsState(initial = false)
    val showAnnotationDialog by viewModel.showAnnotationDialog.collectAsState()
    val editingAnnotation by viewModel.editingAnnotation.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        when (val uiState = state) {
            is ReaderUiState.Loading -> {
                LoadingIndicator(modifier = Modifier.align(alignment = Alignment.Center))
            }

            is ReaderUiState.Error -> {
                ReaderErrorScreen(
                    error = uiState.error,
                    onRetry = { viewModel.retryLoad() },
                )
            }

            is ReaderUiState.Audio -> {
                audioReaderContent(
                    AudioReaderState(
                        uiState = uiState,
                        settingsSheetState = settingsSheetState,
                    ),
                    AudioReaderActions(
                        onSettingsClick = viewModel::openAudiobookSettings,
                        onSettingsChange = viewModel::onSettingsChanged,
                        onSettingsDismiss = viewModel::closeSettings,
                        onNavigateBack = onNavigateBack,
                    ),
                )
            }

            is ReaderUiState.Visual -> {
                visualReaderContent(
                    VisualReaderState(
                        uiState = uiState,
                        settingsSheetState = settingsSheetState,
                        bookmarks = bookmarks,
                        annotations = annotations,
                        searchResults = searchResults,
                        searchQuery = searchQuery,
                        isTtsActive = isTtsActive,
                        isPlaying = isPlaying,
                        showAnnotationDialog = showAnnotationDialog,
                        editingAnnotation = editingAnnotation,
                    ),
                    VisualReaderActions(
                        onSettingsClick = viewModel::openSettings,
                        onSettingsChange = viewModel::onSettingsChanged,
                        onSettingsDismiss = viewModel::closeSettings,
                        onVisualLocatorChanged = viewModel::onVisualLocatorChanged,
                        onNavigatorReady = { viewModel.onNavigatorReady(visualNavigator = it) },
                        onAnnotateAction = viewModel::onAnnotateAction,
                        startTts = viewModel::startTts,
                        stopTts = viewModel::stopTts,
                        play = viewModel::play,
                        pause = viewModel::pause,
                        previous = viewModel::previous,
                        next = viewModel::next,
                        onSearchQueryChanged = viewModel::onSearchQueryChanged,
                        saveAnnotation = { note, color, style ->
                            viewModel.saveAnnotation(
                                note = note,
                                color = color,
                                style = style,
                            )
                        },
                        dismissAnnotationDialog = viewModel::dismissAnnotationDialog,
                        onDeleteAnnotation = { viewModel.deleteAnnotation(id = it.id) },
                        onEditAnnotationAction = viewModel::onEditAnnotationAction,
                        onNavigateBack = onNavigateBack,
                    ),
                )
            }
        }
    }
}

@Composable
private fun DefaultAudioReaderContent(
    state: AudioReaderState,
    actions: AudioReaderActions,
) {
    AudioReader(
        book = state.uiState.book,
        navigator = state.uiState.navigator,
        settingsSheetState = state.settingsSheetState,
        onNavigateBack = actions.onNavigateBack,
        onSettingsClick = actions.onSettingsClick,
        onSettingsChange = actions.onSettingsChange,
        onSettingsDismiss = actions.onSettingsDismiss,
    )
}

@Composable
private fun DefaultVisualReaderContent(
    state: VisualReaderState,
    actions: VisualReaderActions,
) {
    VisualReaderContent(
        uiState = state.uiState,
        settingsSheetState = state.settingsSheetState,
        onSettingsClick = actions.onSettingsClick,
        onSettingsChange = actions.onSettingsChange,
        onSettingsDismiss = actions.onSettingsDismiss,
        bookmarks = state.bookmarks,
        annotations = state.annotations,
        searchResults = state.searchResults,
        searchQuery = state.searchQuery,
        isTtsActive = state.isTtsActive,
        isPlaying = state.isPlaying,
        showAnnotationDialog = state.showAnnotationDialog,
        editingAnnotation = state.editingAnnotation,
        onNavigateBack = actions.onNavigateBack,
        onVisualLocatorChanged = actions.onVisualLocatorChanged,
        onNavigatorReady = actions.onNavigatorReady,
        onAnnotateAction = actions.onAnnotateAction,
        startTts = actions.startTts,
        stopTts = actions.stopTts,
        play = actions.play,
        pause = actions.pause,
        previous = actions.previous,
        next = actions.next,
        onSearchQueryChanged = actions.onSearchQueryChanged,
        saveAnnotation = actions.saveAnnotation,
        dismissAnnotationDialog = actions.dismissAnnotationDialog,
        onDeleteAnnotation = actions.onDeleteAnnotation,
        onEditAnnotation = actions.onEditAnnotationAction,
    )
}
