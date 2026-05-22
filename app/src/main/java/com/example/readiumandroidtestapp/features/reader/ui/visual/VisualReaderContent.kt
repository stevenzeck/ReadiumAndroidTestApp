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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.paging.compose.LazyPagingItems
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import com.example.readiumandroidtestapp.features.reader.ui.components.HighlightDialog
import com.example.readiumandroidtestapp.features.reader.ui.components.ReaderOverlay
import com.example.readiumandroidtestapp.features.reader.ui.components.TocBottomSheet
import com.example.readiumandroidtestapp.features.reader.ui.preferences.SettingsBottomSheet
import com.example.readiumandroidtestapp.features.reader.ui.search.SearchBottomSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderActions
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderNavigator
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferences
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettings
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import kotlinx.coroutines.launch
import org.readium.adapter.pdfium.navigator.PdfiumDefaults
import org.readium.adapter.pdfium.navigator.PdfiumEngineProvider
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.adapter.pdfium.navigator.PdfiumSettings
import org.readium.navigator.common.InputListener
import org.readium.navigator.common.NavigationController
import org.readium.navigator.common.TapContext
import org.readium.navigator.common.TapEvent
import org.readium.navigator.web.fixedlayout.FixedWebGoLocation
import org.readium.navigator.web.fixedlayout.FixedWebRendition
import org.readium.navigator.web.fixedlayout.FixedWebRenditionState
import org.readium.navigator.web.reflowable.ReflowableWebGoLocation
import org.readium.navigator.web.reflowable.ReflowableWebRendition
import org.readium.navigator.web.reflowable.ReflowableWebRenditionState
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

@Composable
fun VisualReaderContent(
    uiState: ReaderUiState.Visual,
    settingsSheetState: ReaderSettings?,
    onSettingsClick: () -> Unit,
    onSettingsChange: (ReaderPreferences) -> Unit,
    onSettingsDismiss: () -> Unit,
    bookmarks: List<Bookmark>,
    highlights: List<Highlight>,
    searchResults: LazyPagingItems<SearchItem>,
    searchQuery: String?,
    isTtsActive: Boolean,
    isPlaying: Boolean,
    showHighlightDialog: Boolean,
    onNavigateBack: () -> Unit,
    onVisualLocatorChanged: (Locator) -> Unit,
    onNavigatorReady: (ReaderNavigator) -> Unit,
    onHighlightAction: (Locator) -> Unit,
    startTts: () -> Unit,
    stopTts: () -> Unit,
    play: () -> Unit,
    pause: () -> Unit,
    previous: () -> Unit,
    next: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    saveHighlight: (String, Int) -> Unit,
    dismissHighlightDialog: () -> Unit,
    pdfNavigatorFactory: PdfNavigatorFactory<PdfiumSettings, PdfiumPreferences, PdfiumPreferencesEditor>? = null,
) {
    var showOverlay by rememberSaveable { mutableStateOf(value = false) }
    var showTocBottomSheet by rememberSaveable { mutableStateOf(value = false) }
    var showSearchBottomSheet by rememberSaveable { mutableStateOf(value = false) }

    var navigator by remember { mutableStateOf<VisualNavigator?>(value = null) }
    val coroutineScope = rememberCoroutineScope()
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

    val inputListener = remember {
        object : InputListener {
            override fun onTap(event: TapEvent, context: TapContext) {
                showOverlay = !showOverlay
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (uiState.publication.conformsTo(profile = Publication.Profile.EPUB)) {
            checkNotNull(uiState.renditionState) { "EPUB rendition state is missing" }

            if (uiState.isFixedLayout) {
                FixedWebRendition(
                    state = uiState.renditionState as FixedWebRenditionState,
                    inputListener = inputListener,
                )
            } else {
                ReflowableWebRendition(
                    state = uiState.renditionState as ReflowableWebRenditionState,
                    inputListener = inputListener,
                )
            }

            // Handle locator changes and readiness
            val controller = uiState.renditionState.controller
            LaunchedEffect(controller) {
                if (controller != null) {
                    onNavigatorReady(ReaderNavigator.New(controller = controller))
                    snapshotFlow { controller.location }
                        .collect { location ->
                            onVisualLocatorChanged(location.toLocator())
                        }
                }
            }
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
                initialPreferences = (uiState.initialPreferences as? ReaderPreferences.Pdf)?.value,
                onLocatorChanged = onVisualLocatorChanged,
                pdfNavigatorFactory = pdfFactory,
                onTap = { showOverlay = !showOverlay },
                onNavigatorReady = {
                    navigator = it
                    onNavigatorReady(ReaderNavigator.Legacy(navigator = it))
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
                highlights = highlights,
                onDismissRequest = { showTocBottomSheet = false },
                onLinkSelected = { link ->
                    val controller = uiState.renditionState?.controller
                    if (controller != null) {
                        coroutineScope.launch {
                            controller.goTo(url = link.url())
                        }
                    } else {
                        navigator?.go(link, animated = true)
                    }
                    showTocBottomSheet = false
                },
                onLocatorSelected = { locator ->
                    val controller = uiState.renditionState?.controller
                    if (controller != null) {
                        coroutineScope.launch {
                            if (uiState.isFixedLayout) {
                                @Suppress("UNCHECKED_CAST")
                                (controller as? NavigationController<*, FixedWebGoLocation>)
                                    ?.goTo(location = FixedWebGoLocation(href = locator.href))
                            } else {
                                @Suppress("UNCHECKED_CAST")
                                (controller as? NavigationController<*, ReflowableWebGoLocation>)
                                    ?.goTo(location = ReflowableWebGoLocation(href = locator.href))
                            }
                        }
                    } else {
                        navigator?.go(locator, animated = true)
                    }
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
                    val controller = uiState.renditionState?.controller
                    if (controller != null) {
                        coroutineScope.launch {
                            if (uiState.isFixedLayout) {
                                @Suppress("UNCHECKED_CAST")
                                (controller as? NavigationController<*, FixedWebGoLocation>)
                                    ?.goTo(location = FixedWebGoLocation(href = locator.href))
                            } else {
                                @Suppress("UNCHECKED_CAST")
                                (controller as? NavigationController<*, ReflowableWebGoLocation>)
                                    ?.goTo(location = ReflowableWebGoLocation(href = locator.href))
                            }
                        }
                    } else {
                        navigator?.go(locator, animated = true)
                    }
                    showSearchBottomSheet = false
                },
            )
        }

        if (settingsSheetState != null) {
            SettingsBottomSheet(
                settings = settingsSheetState,
                onCommit = { wrapped ->
                    onSettingsChange(wrapped)
                },
                onDismissRequest = onSettingsDismiss,
            )
        }

        if (showHighlightDialog) {
            HighlightDialog(
                onDismiss = dismissHighlightDialog,
                onSave = saveHighlight,
            )
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
