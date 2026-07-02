package com.example.readiumandroidtestapp.features.reader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

@Composable
fun TocBottomSheet(
    publication: Publication,
    bookmarks: List<Bookmark>,
    annotations: List<ReaderAnnotation>,
    onDismissRequest: () -> Unit,
    onLinkSelected: (Link) -> Unit,
    onLocatorSelected: (Locator) -> Unit,
    onDeleteAnnotation: (ReaderAnnotation) -> Unit,
    onEditAnnotation: (ReaderAnnotation) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        var selectedTabIndex by remember { mutableIntStateOf(value = 0) }
        val tabs = listOf(
            stringResource(id = R.string.contents),
            stringResource(id = R.string.bookmarks),
            stringResource(id = R.string.annotations),
        )

        Column(modifier = Modifier.fillMaxSize()) {
            SecondaryScrollableTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) },
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> TocChapterList(
                    toc = publication.tableOfContents,
                    onLinkSelected = onLinkSelected,
                )

                1 -> TocBookmarkList(bookmarks = bookmarks, onLocatorSelected = onLocatorSelected)
                2 -> TocAnnotationList(
                    annotations = annotations,
                    onLocatorSelected = onLocatorSelected,
                    onDeleteAnnotation = onDeleteAnnotation,
                    onEditAnnotation = onEditAnnotation,
                )
            }
        }
    }
}

@Composable
private fun TocChapterList(toc: List<Link>, onLinkSelected: (Link) -> Unit) {
    LazyColumn {
        items(items = toc) { link ->
            ListItem(
                modifier = Modifier.clickable { onLinkSelected(link) },
                content = { Text(text = link.title ?: link.href.toString()) },
            )
        }
    }
}

@Composable
private fun TocBookmarkList(bookmarks: List<Bookmark>, onLocatorSelected: (Locator) -> Unit) {
    LazyColumn {
        items(items = bookmarks) { bookmark ->
            ListItem(
                modifier = Modifier.clickable { onLocatorSelected(bookmark.locator) },
                supportingContent = {
                    val progression = bookmark.locator.locations.progression
                    val percentage = if (progression != null) {
                        "${(progression * 100).toInt()}%"
                    } else {
                        ""
                    }
                    Text(text = percentage)
                },
                content = { Text(text = bookmark.resourceTitle) },
            )
        }
    }
}

@Composable
private fun TocAnnotationList(
    annotations: List<ReaderAnnotation>,
    onLocatorSelected: (Locator) -> Unit,
    onDeleteAnnotation: (ReaderAnnotation) -> Unit,
    onEditAnnotation: (ReaderAnnotation) -> Unit,
) {
    LazyColumn {
        items(items = annotations) { annotation ->
            ListItem(
                modifier = Modifier.clickable { onLocatorSelected(annotation.locator) },
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(size = 24.dp)
                            .clip(shape = CircleShape)
                            .background(Color(color = annotation.tint))
                            .border(width = 1.dp, color = Color.Gray, shape = CircleShape),
                    )
                },
                supportingContent = {
                    if (annotation.annotation.isNotEmpty()) {
                        Text(text = annotation.annotation)
                    }
                },
                trailingContent = {
                    Row {
                        IconButton(onClick = { onEditAnnotation(annotation) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.edit),
                                contentDescription = stringResource(id = R.string.edit),
                            )
                        }
                        IconButton(onClick = { onDeleteAnnotation(annotation) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.delete),
                                contentDescription = stringResource(id = R.string.delete),
                            )
                        }
                    }
                },
                content = { Text(text = annotation.locator.text.highlight ?: "") },
            )
        }
    }
}
