package com.example.readiumandroidtestapp.features.reader.ui.components

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.LocalizedString
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Manifest
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

@RunWith(AndroidJUnit4::class)
class ReaderTocTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays chapters and handles click`() {
        val link = Link(href = Url(url = "href")!!, title = "Chapter 1")
        val publication = Publication(
            manifest = Manifest(
                metadata = Metadata(localizedTitle = LocalizedString("Title")),
                tableOfContents = listOf(link),
            ),
        )
        val onLinkSelected = mockk<(Link) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            TocBottomSheet(
                publication = publication,
                bookmarks = emptyList(),
                annotations = emptyList(),
                onDismissRequest = {},
                onLinkSelected = onLinkSelected,
                onLocatorSelected = {},
            )
        }

        composeTestRule.onNodeWithText(text = "Chapter 1").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Chapter 1").performClick()

        verify { onLinkSelected(link) }
    }

    @Test
    fun `displays bookmarks and handles click`() {
        Locator(
            href = Url(url = "href")!!,
            mediaType = MediaType(string = "text/html")!!,
            title = "Locator Title",
        )
        val bookmark = Bookmark(
            id = 1,
            bookId = 1,
            resourceIndex = 0,
            resourceTitle = "Chapter 1",
            resourceHref = "href",
            resourceType = "text/html",
            location = "{}",
            locatorText = "{}",
        )
        val onLocatorSelected = mockk<(Locator) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            TocBottomSheet(
                publication = Publication(
                    manifest = Manifest(
                        metadata = Metadata(
                            localizedTitle = LocalizedString(
                                value = "Title",
                            ),
                        ),
                    ),
                ),
                bookmarks = listOf(bookmark),
                annotations = emptyList(),
                onDismissRequest = {},
                onLinkSelected = {},
                onLocatorSelected = onLocatorSelected,
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val bookmarksTab = context.getString(R.string.bookmarks)

        composeTestRule.onNodeWithText(text = bookmarksTab).performClick()
        composeTestRule.onNodeWithText(text = "Chapter 1").performClick()

        verify { onLocatorSelected(any()) }
    }

    @Test
    fun `displays annotations and handles click`() {
        val locator = Locator(
            href = Url(url = "href")!!,
            mediaType = MediaType(string = "text/html")!!,
            text = Locator.Text(highlight = "Highlighted text"),
        )
        val annotation = ReaderAnnotation(
            bookId = 1,
            style = ReaderAnnotation.Style.HIGHLIGHT,
            tint = 0,
            locator = locator,
            annotation = "",
        )
        val onLocatorSelected = mockk<(Locator) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            TocBottomSheet(
                publication = Publication(
                    manifest = Manifest(
                        metadata = Metadata(
                            localizedTitle = LocalizedString(
                                value = "Title",
                            ),
                        ),
                    ),
                ),
                bookmarks = emptyList(),
                annotations = listOf(annotation),
                onDismissRequest = {},
                onLinkSelected = {},
                onLocatorSelected = onLocatorSelected,
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val annotationsTab = context.getString(R.string.annotations)

        composeTestRule.onNodeWithText(text = annotationsTab).performClick()
        composeTestRule.onNodeWithText(text = "Highlighted text").performClick()

        verify { onLocatorSelected(any()) }
    }
}
