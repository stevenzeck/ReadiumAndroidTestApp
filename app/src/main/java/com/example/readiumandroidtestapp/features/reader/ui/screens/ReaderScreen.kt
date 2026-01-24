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
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.readiumandroidtestapp.features.reader.ui.audio.AudioReader
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.visual.VisualReaderContent

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
) {

    // State Collection
    val state by viewModel.uiState.collectAsState()
    val settingsSheetState by viewModel.settingsSheetState.collectAsState()

    // Auxiliary State (Bookmarks, Highlights, Search, TTS)
    val bookmarks by viewModel.bookmarks.collectAsState(initial = emptyList())
    val highlights by viewModel.highlights.collectAsState(initial = emptyList())
    val searchResults = viewModel.searchResults.collectAsLazyPagingItems()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isTtsActive by viewModel.isTtsActive.collectAsState()
    val isPlaying by viewModel.ttsPlayback.collectAsState(initial = false)
    val showHighlightDialog by viewModel.showHighlightDialog.collectAsState()

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
                // Delegate to Audio Reader UI
                AudioReader(
                    book = uiState.book,
                    navigator = uiState.navigator,
                    settingsSheetState = settingsSheetState,
                    onNavigateBack = onNavigateBack,
                    onSettingsClick = viewModel::openAudiobookSettings,
                    onSettingsChange = viewModel::onSettingsChanged,
                    onSettingsDismiss = viewModel::closeSettings,
                )
            }

            is ReaderUiState.Visual -> {
                // Delegate to Visual Reader UI (Epub, PDF)
                // We hoist the 'onNavigatorReady' callback here to give the ViewModel access
                // to the underlying Readium VisualNavigator instance for controlling things like TTS.
                VisualReaderContent(
                    uiState = uiState,
                    settingsSheetState = settingsSheetState,
                    onSettingsClick = viewModel::openSettings,
                    onSettingsChange = viewModel::onSettingsChanged,
                    onSettingsDismiss = viewModel::closeSettings,
                    bookmarks = bookmarks,
                    highlights = highlights,
                    searchResults = searchResults,
                    searchQuery = searchQuery,
                    isTtsActive = isTtsActive,
                    isPlaying = isPlaying,
                    showHighlightDialog = showHighlightDialog,
                    onNavigateBack = onNavigateBack,
                    onVisualLocatorChanged = viewModel::onVisualLocatorChanged,
                    onNavigatorReady = { viewModel.onNavigatorReady(visualNavigator = it) },
                    onHighlightAction = viewModel::onHighlightAction,
                    startTts = viewModel::startTts,
                    stopTts = viewModel::stopTts,
                    play = viewModel::play,
                    pause = viewModel::pause,
                    previous = viewModel::previous,
                    next = viewModel::next,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    saveHighlight = { note, color ->
                        viewModel.saveHighlight(
                            note = note,
                            color = color,
                        )
                    },
                    dismissHighlightDialog = viewModel::dismissHighlightDialog,
                )
            }
        }
    }
}
