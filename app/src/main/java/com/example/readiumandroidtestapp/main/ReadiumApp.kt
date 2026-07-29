package com.example.readiumandroidtestapp.main

import android.annotation.SuppressLint
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.navigation.LocalNavigator
import com.example.readiumandroidtestapp.core.navigation.Navigator
import com.example.readiumandroidtestapp.core.navigation.rememberNavigationState
import com.example.readiumandroidtestapp.core.navigation.route.Account
import com.example.readiumandroidtestapp.core.navigation.route.Bookshelf
import com.example.readiumandroidtestapp.core.navigation.route.Catalogs
import com.example.readiumandroidtestapp.core.navigation.route.NavEntryBuilder
import com.example.readiumandroidtestapp.core.navigation.route.Reader
import com.example.readiumandroidtestapp.core.navigation.toEntries
import com.example.readiumandroidtestapp.features.reader.ui.audio.ExpandableAudioPlayer
import com.example.readiumandroidtestapp.features.reader.ui.audio.components.AudioMiniPlayer
import kotlinx.coroutines.launch

/**
 * The main entry point composable for the application (The "Shell").
 *
 * This composable acts as the orchestrator for the application's UI, handling:
 * 1. **Navigation State Management**: Initializing and persisting back stacks.
 * 2. **Adaptive Layout**: Switching between standard navigation (Navigation Rail/Bar) and full-screen modes.
 * 3. **Feature Integration**: Aggregating navigation entries from decoupled modules.
 * 4. **Global UI Elements**: Managing global snackbars and entry transitions.
 *
 * @param entryBuilders A set of [NavEntryBuilder] functions injected via Hilt Multibindings.
 *                      This allows features (Bookshelf, Reader, etc.) to plug their own navigation
 *                      logic into the shell without the shell needing hardcoded dependencies on them.
 * @param viewModel The global [MainViewModel] used for handling app-wide events like snackbar messages.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadiumApp(
    entryBuilders: Set<NavEntryBuilder>,
    viewModel: MainViewModel = hiltViewModel(),
    initialBookId: Long? = null,
    initialIsAudiobook: Boolean = false,
) {
    // 1. Initialize Navigation State
    // We explicitly define the start route (Bookshelf) and the top-level tabs.
    val navigationState = rememberNavigationState(
        startRoute = Bookshelf,
        topLevelRoutes = listOf(Bookshelf, Catalogs, Account),
    )
    // Create the Navigator helper and remember it keyed to the state.
    val navigator = remember(navigationState) { Navigator(navigationState) }

    val currentRoute = navigationState.topLevelRoute
    val currentDestination = navigationState.currentDestination
    val isReaderMode = currentDestination is Reader && !currentDestination.isAudiobook

    LaunchedEffect(initialBookId) {
        if (initialBookId != null) {
            navigator.navigate(
                route = Reader(
                    bookId = initialBookId,
                    isAudiobook = initialIsAudiobook,
                ),
            )
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 2. Lifecycle Integration for User Messages
    // We observe the message flow from the ViewModel and display them using the global SnackbarHost.
    // flowWithLifecycle ensures collection stops when the app is in the background.
    val messages = viewModel.userMessages
    LaunchedEffect(messages) {
        messages.flowWithLifecycle(lifecycleOwner.lifecycle).collect { messageId ->
            @SuppressLint("LocalContextGetResourceValueCall") snackbarHostState.showSnackbar(
                message = context.getString(messageId),
            )
        }
    }

    // 3. Navigation Entry Aggregation
    // The entryProvider DSL builds the routing map. We iterate through the injected builders
    // to populate this map dynamically.
    val entryProvider = entryProvider {
        entryBuilders.forEach { it() }
    }

    // Provide the Navigator to the entire composition tree.
    CompositionLocalProvider(value = LocalNavigator provides navigator) {

        // 4. Adaptive Layout Logic
        // We switch layout strategies based on the current destination.
        // IF the user is in the Reader, we want a fully immersive, edge-to-edge experience (No navigation bars).
        // ELSE, we use the standard NavigationSuiteScaffold (Adaptive Nav Rail/Bottom Bar).
        if (isReaderMode) {
            val activity = LocalActivity.current
            Box(
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                // Feature Content Display
                NavDisplay(
                    entries = navigationState.toEntries(entryProvider = entryProvider),
                    onBack = { if (!navigator.goBack()) activity?.finish() },
                    modifier = Modifier.fillMaxSize(),
                    transitionSpec = {
                        val initialKey = initialState.key
                        val targetKey = targetState.key
                        // Adaptive Transitions:
                        // - Cross-fade for standard transitions.
                        // - Instant transition between top-level tabs (Standard Nav behavior).
                        if (initialKey in navigationState.topLevelDestinations && targetKey in navigationState.topLevelDestinations) {
                            EnterTransition.None togetherWith ExitTransition.None
                        } else {
                            fadeIn() togetherWith fadeOut()
                        }
                    },
                    popTransitionSpec = {
                        val initialKey = initialState.key
                        val targetKey = targetState.key
                        if (initialKey in navigationState.topLevelDestinations && targetKey in navigationState.topLevelDestinations) {
                            EnterTransition.None togetherWith ExitTransition.None
                        } else {
                            fadeIn() togetherWith fadeOut()
                        }
                    },
                )
                // Global Snackbar (re-positioned for Reader mode if necessary, currently bottom aligned)
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }

        } else {
            // Standard App UI Shell
            NavigationSuiteScaffold(
                layoutType = NavigationSuiteScaffoldDefaults.calculateFromAdaptiveInfo(
                    adaptiveInfo = currentWindowAdaptiveInfo(),
                ),
                navigationSuiteItems = {
                    navigationState.topLevelDestinations.forEach { screen ->
                        val isSelected = currentRoute == screen

                        val (titleResId, iconResId) = when (screen) {
                            Bookshelf -> R.string.bookshelf to if (isSelected) R.drawable.shelves_filled else R.drawable.shelves
                            Catalogs -> R.string.catalogs to if (isSelected) R.drawable.browse_filled else R.drawable.browse
                            Account -> R.string.account to if (isSelected) R.drawable.account_circle_filled else R.drawable.account_circle
                            else -> R.string.empty to R.drawable.info
                        }

                        item(
                            icon = {
                                Icon(
                                    painter = painterResource(id = iconResId),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            },
                            label = { Text(text = stringResource(id = titleResId)) },
                            selected = currentRoute == screen,
                            onClick = { navigator.navigate(route = screen) },
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                // Inner Scaffold handles padding and global UI elements like Snackbars within the standard shell.
                Scaffold(
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                ) { innerPadding ->
                    val activity = LocalActivity.current
                    val activeBook by viewModel.activeAudioBook.collectAsState()
                    val audioNavigator by viewModel.audioNavigator.collectAsState()
                    val audioEditor by viewModel.audioPreferencesEditor.collectAsState()
                    val audioPublication by viewModel.audioPublication.collectAsState()

                    val currentBook = activeBook
                    val currentNavigator = audioNavigator
                    val currentEditor = audioEditor
                    val currentPublication = audioPublication

                    val bottomSheetState = rememberBottomSheetState(
                        initialValue = SheetValue.PartiallyExpanded,
                        enabledValues = setOf(
                            SheetValue.PartiallyExpanded,
                            SheetValue.Expanded,
                        ),
                    )
                    val scaffoldState = rememberBottomSheetScaffoldState(
                        bottomSheetState = bottomSheetState,
                    )
                    val scope = rememberCoroutineScope()

                    LaunchedEffect(currentBook, currentNavigator) {
                        if (currentBook == null || currentNavigator == null) {
                            // Close or hide the sheet if needed, though we'll control visibility via peekHeight
                        }
                    }

                    LaunchedEffect(Unit) {
                        viewModel.expandPlayerEvent.collect {
                            if (currentBook != null) {
                                bottomSheetState.expand()
                            }
                        }
                    }

                    BottomSheetScaffold(
                        scaffoldState = scaffoldState,
                        sheetPeekHeight = if (currentBook != null && currentNavigator != null) 80.dp else 0.dp,
                        sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        sheetDragHandle = null,
                        sheetShape = RectangleShape,
                        sheetContent = {
                            if (currentBook != null && currentNavigator != null && currentEditor != null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    ExpandableAudioPlayer(
                                        book = currentBook,
                                        publication = currentPublication,
                                        navigator = currentNavigator,
                                        editor = currentEditor,
                                        sheetState = bottomSheetState,
                                        onExpand = { scope.launch { bottomSheetState.expand() } },
                                        onCollapse = { scope.launch { bottomSheetState.partialExpand() } },
                                    )
                                }
                            } else {
                                Box(modifier = Modifier.fillMaxSize()) // Empty content when not visible
                            }
                        },
                    ) { _ ->
                        Box(
                            modifier = Modifier
                                .padding(paddingValues = innerPadding)
                                .consumeWindowInsets(paddingValues = innerPadding),
                        ) {
                            NavDisplay(
                                entries = navigationState.toEntries(entryProvider = entryProvider),
                                onBack = { if (!navigator.goBack()) activity?.finish() },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = if (currentBook != null && currentNavigator != null) 80.dp else 0.dp),
                                transitionSpec = {
                                    val initialKey = initialState.key
                                    val targetKey = targetState.key
                                    if (initialKey in navigationState.topLevelDestinations && targetKey in navigationState.topLevelDestinations) {
                                        EnterTransition.None togetherWith ExitTransition.None
                                    } else {
                                        fadeIn() togetherWith fadeOut()
                                    }
                                },
                                popTransitionSpec = {
                                    val initialKey = initialState.key
                                    val targetKey = targetState.key
                                    if (initialKey in navigationState.topLevelDestinations && targetKey in navigationState.topLevelDestinations) {
                                        EnterTransition.None togetherWith ExitTransition.None
                                    } else {
                                        fadeIn() togetherWith fadeOut()
                                    }
                                },
                            )

                            if (currentBook != null && currentNavigator != null) {
                                AudioMiniPlayer(
                                    book = currentBook,
                                    publication = currentPublication,
                                    navigator = currentNavigator,
                                    onClick = { scope.launch { bottomSheetState.expand() } },
                                    modifier = Modifier.align(Alignment.BottomCenter),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
