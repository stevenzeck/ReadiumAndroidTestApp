package com.example.readiumandroidtestapp.features.reader.ui.visual

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.paging.compose.LazyPagingItems
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import com.example.readiumandroidtestapp.features.reader.ui.components.AnnotationDialog
import com.example.readiumandroidtestapp.features.reader.ui.components.ReaderOverlay
import com.example.readiumandroidtestapp.features.reader.ui.components.TocBottomSheet
import com.example.readiumandroidtestapp.features.reader.ui.preferences.SettingsBottomSheet
import com.example.readiumandroidtestapp.features.reader.ui.search.SearchBottomSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderActions
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import org.readium.adapter.pdfium.navigator.PdfiumDefaults
import org.readium.adapter.pdfium.navigator.PdfiumEngineProvider
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.adapter.pdfium.navigator.PdfiumSettings
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

@Composable
fun VisualReaderContent(
    uiState: ReaderUiState.Visual,
    settingsSheetState: ReaderSettingsSheet?,
    onSettingsClick: () -> Unit,
    onSettingsChange: (Configurable.Preferences<*>) -> Unit,
    onSettingsDismiss: () -> Unit,
    bookmarks: List<Bookmark>,
    annotations: List<ReaderAnnotation>,
    searchResults: LazyPagingItems<SearchItem>,
    searchQuery: String?,
    isTtsActive: Boolean,
    isPlaying: Boolean,
    showAnnotationDialog: Boolean,
    editingAnnotation: ReaderAnnotation?,
    onNavigateBack: () -> Unit,
    onVisualLocatorChanged: (Locator) -> Unit,
    onNavigatorReady: (VisualNavigator) -> Unit,
    onAnnotateAction: (Locator) -> Unit,
    startTts: () -> Unit,
    stopTts: () -> Unit,
    play: () -> Unit,
    pause: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    saveAnnotation: (String, Int, ReaderAnnotation.Style) -> Unit,
    dismissAnnotationDialog: () -> Unit,
    onDeleteAnnotation: (ReaderAnnotation) -> Unit,
    onEditAnnotation: (ReaderAnnotation) -> Unit,
    epubNavigatorFactory: EpubNavigatorFactory? = null,
    pdfNavigatorFactory: PdfNavigatorFactory<PdfiumSettings, PdfiumPreferences, PdfiumPreferencesEditor>? = null,
) {
    var showOverlay by rememberSaveable { mutableStateOf(value = false) }
    var showTocBottomSheet by rememberSaveable { mutableStateOf(value = false) }
    var showSearchBottomSheet by rememberSaveable { mutableStateOf(value = false) }

    var navigator by remember { mutableStateOf<VisualNavigator?>(value = null) }
    val view = LocalView.current
    if (!view.isInEditMode) {
        val currentWindow = (view.context.findActivity())?.window
        if (currentWindow != null) {
            LaunchedEffect(showOverlay) {
                val insetsController = WindowCompat.getInsetsController(currentWindow, view)
                insetsController.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

                if (showOverlay) {
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                } else {
                    insetsController.hide(WindowInsetsCompat.Type.systemBars())
                }
            }

            DisposableEffect(Unit) {
                onDispose {
                    val insetsController = WindowCompat.getInsetsController(currentWindow, view)
                    insetsController.show(WindowInsetsCompat.Type.systemBars())
                }
            }
        }
    }

    val actions = remember(navigator, isTtsActive, isPlaying) {
        ReaderActions(
            onNavigateBack = onNavigateBack,
            onSearchClick = { showSearchBottomSheet = true },
            onTtsClick = startTts,
            onSettingsClick = onSettingsClick,
            onTocClick = { showTocBottomSheet = true },
            onTtsPlayPause = { if (isPlaying) pause() else play() },
            onTtsPrevious = previous,
            onTtsNext = next,
            onTtsStop = stopTts,
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.publication.conformsTo(profile = Publication.Profile.EPUB)) {
            EpubReader(
                publication = uiState.publication,
                initialLocator = uiState.initialLocator,
                initialPreferences = uiState.initialPreferences as EpubPreferences,
                onLocatorChanged = onVisualLocatorChanged,
                onTap = { showOverlay = !showOverlay },
                onNavigatorReady = {
                    navigator = it
                    onNavigatorReady(it)
                },
                onAnnotate = onAnnotateAction,
                epubNavigatorFactory = epubNavigatorFactory,
            )
        } else if (uiState.publication.conformsTo(profile = Publication.Profile.PDF)) {
            val pdfFactory = pdfNavigatorFactory ?: remember(uiState.pdfiumDocumentFactory) {
                PdfNavigatorFactory(
                    publication = uiState.publication,
                    pdfEngineProvider = PdfiumEngineProvider(
                        defaults = PdfiumDefaults(),
                    ),
                )
            }

            PdfReader(
                publication = uiState.publication,
                initialLocator = uiState.initialLocator,
                initialPreferences = uiState.initialPreferences as? PdfiumPreferences,
                onLocatorChanged = onVisualLocatorChanged,
                pdfNavigatorFactory = pdfFactory,
                onTap = { showOverlay = !showOverlay },
                onNavigatorReady = {
                    navigator = it
                    onNavigatorReady(it)
                },
            )
        } else {
            Text(
                text = "Unsupported format",
                modifier = Modifier.align(alignment = Alignment.Center),
            )
        }

        ReaderOverlay(
            visible = showOverlay,
            title = uiState.publication.metadata.title,
            capabilities = uiState.capabilities,
            isTtsActive = isTtsActive,
            isPlaying = isPlaying,
            actions = actions,
        )

        if (showTocBottomSheet) {
            TocBottomSheet(
                publication = uiState.publication,
                bookmarks = bookmarks,
                annotations = annotations,
                onDismissRequest = { showTocBottomSheet = false },
                onLinkSelected = { link ->
                    navigator?.go(link, animated = true)
                    showTocBottomSheet = false
                },
                onLocatorSelected = { locator ->
                    navigator?.go(locator, animated = true)
                    showTocBottomSheet = false
                },
                onDeleteAnnotation = onDeleteAnnotation,
                onEditAnnotation = { annotation ->
                    onEditAnnotation(annotation)
                    showTocBottomSheet = false
                },
            )
        }

        if (showSearchBottomSheet) {
            SearchBottomSheet(
                query = searchQuery,
                onQueryChange = onSearchQueryChanged,
                results = searchResults,
                onDismissRequest = { showSearchBottomSheet = false },
                onLocatorSelected = { locator ->
                    navigator?.go(locator, animated = true)
                    showSearchBottomSheet = false
                },
            )
        }

        if (settingsSheetState != null) {
            val settingsObject = when (settingsSheetState) {
                is ReaderSettingsSheet.Tts -> settingsSheetState.session
                is ReaderSettingsSheet.Configurable -> settingsSheetState.editor
            }

            SettingsBottomSheet(
                settings = settingsObject,
                isFixedLayout = uiState.isFixedLayout,
                onCommit = { preferences -> onSettingsChange(preferences) },
                onDismissRequest = onSettingsDismiss,
            )
        }

        if (showAnnotationDialog) {
            AnnotationDialog(
                annotation = editingAnnotation,
                onDismiss = dismissAnnotationDialog,
                onSave = saveAnnotation,
            )
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
