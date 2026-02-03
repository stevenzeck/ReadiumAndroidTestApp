package com.example.readiumandroidtestapp.features.catalogs.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.BackNavigationBehavior
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.navigation.route.CatalogScreens
import com.example.readiumandroidtestapp.features.catalogs.ui.detail.CatalogDetailScreen
import com.example.readiumandroidtestapp.features.catalogs.ui.feed.CatalogFeedScreen
import com.example.readiumandroidtestapp.features.catalogs.ui.publication.PublicationDetailScreen
import kotlinx.coroutines.launch
import org.readium.r2.shared.publication.Publication

@Composable
fun CatalogsScreen(
    feedScreen: @Composable (onCatalogClick: (Catalog) -> Unit) -> Unit = { onCatalogClick ->
        CatalogFeedScreen(onCatalogClick = onCatalogClick)
    },
    detailScreen: @Composable (Catalog, () -> Unit, Boolean, (Catalog) -> Unit, (Publication) -> Unit) -> Unit = { catalog, onNavigateBack, showBackButton, onSubFeedClick, onPublicationClick ->
        CatalogDetailScreen(
            catalog = catalog,
            onNavigateBack = onNavigateBack,
            showBackButton = showBackButton,
            onSubFeedClick = onSubFeedClick,
            onPublicationClick = onPublicationClick,
        )
    },
    publicationScreen: @Composable (String, () -> Unit) -> Unit = { manifestJson, onNavigateBack ->
        PublicationDetailScreen(
            manifestJson = manifestJson,
            onNavigateBack = onNavigateBack,
        )
    },
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<CatalogScreens>()
    val backNavigationBehavior = BackNavigationBehavior.PopUntilContentChange
    val scope = rememberCoroutineScope()

    val isListAndDetailVisible =
        navigator.scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded &&
                navigator.scaffoldValue[ListDetailPaneScaffoldRole.Detail] == PaneAdaptedValue.Expanded

    val currentScreen = navigator.currentDestination?.contentKey
    val isRootCatalog = (currentScreen as? CatalogScreens.CatalogDetail)?.catalog?.id != null

    val isInternalBackEnabled = navigator.canNavigateBack(backNavigationBehavior) &&
            !(isListAndDetailVisible && isRootCatalog)

    BackHandler(enabled = isInternalBackEnabled) {
        scope.launch { navigator.navigateBack(backNavigationBehavior) }
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        defaultBackBehavior = backNavigationBehavior,
        listPane = {
            AnimatedPane {
                feedScreen { catalog ->
                    scope.launch {
                        navigator.navigateTo(
                            pane = ListDetailPaneScaffoldRole.Detail,
                            contentKey = CatalogScreens.CatalogDetail(catalog),
                        )
                    }
                }
            }
        },
        detailPane = {
            AnimatedPane {
                val selectedScreen = navigator.currentDestination?.contentKey

                AnimatedContent(
                    targetState = selectedScreen,
                    label = "DetailPaneTransition",
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(durationMillis = 220, delayMillis = 90)) +
                                scaleIn(
                                    initialScale = 0.92f,
                                    animationSpec = tween(durationMillis = 220, delayMillis = 90),
                                ))
                            .togetherWith(exit = fadeOut(animationSpec = tween(durationMillis = 90)))
                    },
                ) { screen ->
                    when (screen) {
                        is CatalogScreens.CatalogDetail -> {
                            detailScreen(
                                screen.catalog,
                                {
                                    scope.launch {
                                        navigator.navigateBack(backNavigationBehavior)
                                    }
                                },
                                isInternalBackEnabled,
                                { subCatalog ->
                                    scope.launch {
                                        navigator.navigateTo(
                                            pane = ListDetailPaneScaffoldRole.Detail,
                                            contentKey = CatalogScreens.CatalogDetail(subCatalog),
                                        )
                                    }
                                },
                                { publication ->
                                    scope.launch {
                                        // Serialize Manifest to JSON String for safe arguments
                                        val jsonString = publication.manifest.toJSON().toString()

                                        navigator.navigateTo(
                                            pane = ListDetailPaneScaffoldRole.Detail,
                                            contentKey = CatalogScreens.PublicationDetail(
                                                manifestJson = jsonString,
                                            ),
                                        )
                                    }
                                },
                            )
                        }

                        is CatalogScreens.PublicationDetail -> {
                            publicationScreen(
                                screen.manifestJson,
                            ) {
                                scope.launch {
                                    navigator.navigateBack(backNavigationBehavior)
                                }
                            }
                        }

                        null -> {
                            EmptyDetailState()
                        }
                    }
                }
            }
        },
    )
}

@Composable
private fun EmptyDetailState() {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                painter = painterResource(id = R.drawable.browse),
                contentDescription = null,
                modifier = Modifier.padding(bottom = 8.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(id = R.string.no_feed_selected),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(id = R.string.select_feed_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}
