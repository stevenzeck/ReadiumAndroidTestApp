package com.example.readiumandroidtestapp.features.reader.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.core.domain.model.Highlight
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ReaderTocTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `TocBottomSheet displays Contents tab by default`() {
        val link = Link(href = Url(url = "chapter1.html")!!, title = "Chapter 1")
        val publication = mockk<Publication>(relaxed = true) {
            every { tableOfContents } returns listOf(link)
        }

        composeTestRule.setContent {
            TocBottomSheet(
                publication = publication,
                bookmarks = emptyList(),
                highlights = emptyList(),
                onDismissRequest = {},
                onLinkSelected = {},
                onLocatorSelected = {},
            )
        }

        composeTestRule.onNodeWithText(text = "Contents").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Chapter 1").assertIsDisplayed()
    }

    @Test
    fun `TocBottomSheet switches to Bookmarks tab`() {
        val bookmark = Bookmark(
            id = 1L,
            creation = 0L,
            bookId = 1L,
            resourceIndex = 0L,
            resourceHref = "chap1",
            resourceType = "text/html",
            resourceTitle = "Bookmark 1",
            location = "{}",
            locatorText = "{}",
        )
        val publication = mockk<Publication>(relaxed = true)

        composeTestRule.setContent {
            TocBottomSheet(
                publication = publication,
                bookmarks = listOf(bookmark),
                highlights = emptyList(),
                onDismissRequest = {},
                onLinkSelected = {},
                onLocatorSelected = {},
            )
        }

        composeTestRule.onNodeWithText(text = "Bookmarks").performClick()
        composeTestRule.onNodeWithText(text = "Bookmark 1").assertIsDisplayed()
    }

    @Test
    fun `TocBottomSheet switches to Highlights tab`() {
        val locator = Locator(
            href = Url(url = "chap1")!!,
            mediaType = MediaType(string = "text/html")!!,
            text = Locator.Text(highlight = "Highlighted text"),
        )
        val highlight = Highlight(
            bookId = 1L,
            style = Highlight.Style.HIGHLIGHT,
            tint = 0,
            locator = locator,
            annotation = "My Annotation",
        )
        val publication = mockk<Publication>(relaxed = true)

        composeTestRule.setContent {
            TocBottomSheet(
                publication = publication,
                bookmarks = emptyList(),
                highlights = listOf(highlight),
                onDismissRequest = {},
                onLinkSelected = {},
                onLocatorSelected = {},
            )
        }

        composeTestRule.onNodeWithText(text = "Highlights").performClick()
        composeTestRule.onNodeWithText(text = "Highlighted text").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "My Annotation").assertIsDisplayed()
    }
}
