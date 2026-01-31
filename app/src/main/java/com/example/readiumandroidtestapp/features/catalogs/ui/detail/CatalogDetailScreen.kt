package com.example.readiumandroidtestapp.features.catalogs.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.app.AppViewModel
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.ui.common.BookItem
import com.example.readiumandroidtestapp.core.ui.common.ReadiumScaffold
import org.readium.r2.shared.opds.Group
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.opds.images

@Composable
fun CatalogDetailScreen(
    catalog: Catalog,
    onNavigateBack: () -> Unit,
    showBackButton: Boolean,
    onSubFeedClick: (Catalog) -> Unit,
    onPublicationClick: (Publication) -> Unit,
    viewModel: CatalogDetailViewModel = hiltViewModel(
        creationCallback = { factory: CatalogDetailViewModel.Factory ->
            factory.create(catalog)
        },
    ),
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val feedState by viewModel.feedState.collectAsState()

    CatalogDetailContent(
        feedState = feedState,
        catalog = catalog,
        showBackButton = showBackButton,
        onNavigateBack = onNavigateBack,
        onSubFeedClick = onSubFeedClick,
        onPublicationClick = onPublicationClick,
        onImportBook = appViewModel::importBook,
    )
}

@Composable
fun CatalogDetailContent(
    feedState: FeedState,
    catalog: Catalog,
    showBackButton: Boolean,
    onNavigateBack: () -> Unit,
    onSubFeedClick: (Catalog) -> Unit,
    onPublicationClick: (Publication) -> Unit,
    onImportBook: (String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val navigationTitle = stringResource(id = R.string.navigation)
    val publicationsTitle = stringResource(id = R.string.publications)
    val noFeedDataString = stringResource(id = R.string.error_no_feed_data)
    val parsingErrorString = stringResource(id = R.string.error_parsing_feed)

    ReadiumScaffold(
        title = catalog.title,
        scrollBehavior = scrollBehavior,
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        navigationIcon = {
            if (showBackButton) {
                IconButton(
                    onClick = onNavigateBack,
                    shape = RoundedCornerShape(size = 12.dp),
                    modifier = Modifier.padding(start = 8.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = stringResource(id = R.string.back),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            when (feedState) {
                is FeedState.Loading -> {
                    LoadingIndicator(modifier = Modifier.align(alignment = Alignment.Center))
                }

                is FeedState.Success -> {
                    val feed = feedState.feed
                    if (feed != null) {
                        LazyVerticalGrid(
                            columns = GridCells.Adaptive(minSize = 160.dp),
                            contentPadding = PaddingValues(all = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(space = 0.dp),
                            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            // Navigation (Top-level folders)
                            if (feed.navigation.isNotEmpty()) {
                                renderSectionHeader(title = navigationTitle)
                                renderNavigationLinks(
                                    links = feed.navigation,
                                    parentCatalog = catalog,
                                    onSubFeedClick = onSubFeedClick,
                                )
                            }

                            // Groups
                            if (feed.groups.isNotEmpty()) {
                                feed.groups.forEach { group ->
                                    renderGroup(
                                        group = group,
                                        parentCatalog = catalog,
                                        onSubFeedClick = onSubFeedClick,
                                        onPublicationClick = onPublicationClick,
                                        onImportBook = onImportBook,
                                    )
                                }
                            }

                            // Publications
                            if (feed.publications.isNotEmpty()) {
                                renderSectionHeader(title = publicationsTitle)
                                renderPublications(
                                    publications = feed.publications,
                                    onPublicationClick = onPublicationClick,
                                    onImportBook = onImportBook,
                                )
                            }
                        }
                    } else {
                        Text(
                            text = noFeedDataString,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(alignment = Alignment.Center),
                        )
                    }
                }

                is FeedState.Error -> {
                    val message = feedState.message ?: parsingErrorString
                    Text(
                        text = message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(alignment = Alignment.Center),
                    )
                }
            }
        }
    }
}

/**
 * Renders a full section title occupying the full width.
 */
private fun LazyGridScope.renderSectionHeader(
    title: String,
    onHeaderClick: (() -> Unit)? = null,
) {
    item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 8.dp)
                .then(
                    if (onHeaderClick != null) Modifier.clickable(onClick = onHeaderClick)
                    else Modifier,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(weight = 1f),
            )
            if (onHeaderClick != null) {
                FilledTonalIconButton(
                    onClick = onHeaderClick,
                    shape = RoundedCornerShape(size = 12.dp),
                    modifier = Modifier.size(size = 32.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_forward),
                        contentDescription = stringResource(id = R.string.group_link_description),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Renders a list of navigation links (folders).
 */
private fun LazyGridScope.renderNavigationLinks(
    links: List<Link>,
    parentCatalog: Catalog,
    onSubFeedClick: (Catalog) -> Unit,
) {
    items(
        items = links,
        span = { GridItemSpan(currentLineSpan = maxLineSpan) },
    ) { link ->
        val defaultSubFeedTitle = stringResource(id = R.string.sub_feed)
        ListItem(
            headlineContent = {
                Text(text = link.title ?: link.href.toString())
            },
            leadingContent = {
                Icon(
                    painter = painterResource(id = R.drawable.shelves),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            modifier = Modifier.clickable {
                val subCatalog = Catalog(
                    title = link.title ?: defaultSubFeedTitle,
                    href = link.href.toString(),
                    type = parentCatalog.type,
                )
                onSubFeedClick(subCatalog)
            },
        )
    }
}

/**
 * Renders a list of publications (books).
 */
private fun LazyGridScope.renderPublications(
    publications: List<Publication>,
    onPublicationClick: (Publication) -> Unit,
    onImportBook: (String) -> Unit,
) {
    items(items = publications) { publication ->
        PublicationItem(
            publication = publication,
            onPublicationClick = onPublicationClick,
            onImportBook = onImportBook,
            modifier = Modifier.padding(bottom = 16.dp),
        )
    }
}

@Composable
fun PublicationItem(
    publication: Publication,
    onPublicationClick: (Publication) -> Unit,
    onImportBook: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val coverUrl = publication.images.firstOrNull()?.href?.toString()
    BookItem(
        title = publication.metadata.title ?: stringResource(id = R.string.unknown_title),
        coverModel = coverUrl,
        modifier = modifier,
        onClick = { onPublicationClick(publication) },
        menuContent = { dismiss ->
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(id = R.string.download),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                onClick = {
                    val acquisitionLink = publication.links.firstOrNull { link ->
                        link.rels.any { it.startsWith("http://opds-spec.org/acquisition") } || link.mediaType?.isOpds == true
                    }
                    val acquisitionUrl = acquisitionLink?.href?.toString()

                    if (acquisitionUrl != null) {
                        onImportBook(acquisitionUrl)
                    }
                    dismiss()
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.download),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                contentPadding = MenuDefaults.DropdownMenuItemContentPadding,
            )
        },
    )
}

/**
 * Renders an OPDS Group. Groups can contain navigation links, publications, or both.
 */
private fun LazyGridScope.renderGroup(
    group: Group,
    parentCatalog: Catalog,
    onSubFeedClick: (Catalog) -> Unit,
    onPublicationClick: (Publication) -> Unit,
    onImportBook: (String) -> Unit,
) {
    val title = group.metadata.title
    val selfLink = group.links.firstOrNull { it.rels.contains("self") } ?: group.links.firstOrNull()

    renderSectionHeader(
        title = title,
        onHeaderClick = if (selfLink != null) {
            {
                val subCatalog = Catalog(
                    title = title,
                    href = selfLink.href.toString(),
                    type = parentCatalog.type,
                )
                onSubFeedClick(subCatalog)
            }
        } else null,
    )

    if (group.navigation.isNotEmpty()) {
        renderNavigationLinks(
            links = group.navigation,
            parentCatalog = parentCatalog,
            onSubFeedClick = onSubFeedClick,
        )
    }

    if (group.publications.isNotEmpty()) {
        item(span = { GridItemSpan(currentLineSpan = maxLineSpan) }) {
            LazyRow(
                contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                items(items = group.publications) { publication ->
                    PublicationItem(
                        publication = publication,
                        onPublicationClick = onPublicationClick,
                        onImportBook = onImportBook,
                        modifier = Modifier.width(width = 160.dp),
                    )
                }
            }
        }
    }
}
