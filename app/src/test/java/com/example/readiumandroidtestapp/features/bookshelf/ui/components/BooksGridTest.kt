package com.example.readiumandroidtestapp.features.bookshelf.ui.components

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.readiumandroidtestapp.core.domain.model.Book
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w1080dp-h1920dp")
class BooksGridTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `BooksGrid displays list of books`() {
        val books = listOf(
            Book(
                id = 1,
                title = "Book 1",
                href = "href1",
                identifier = "id1",
                mediaType = MediaType(string = "application/epub+zip")!!,
                cover = null,
            ),
            Book(
                id = 2,
                title = "Book 2",
                href = "href2",
                identifier = "id2",
                mediaType = MediaType(string = "application/epub+zip")!!,
                cover = null,
            ),
        )

        composeTestRule.setContent {
            BooksGrid(
                books = books,
                onBookClick = {},
                onMenuClick = {},
            )
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText(text = "Book 1").assertExists()
        composeTestRule.onNodeWithText(text = "Book 2").assertExists()
    }

    @Test
    fun `onBookClick is called when a book is clicked`() {
        val books = listOf(
            Book(
                id = 1,
                title = "Book 1",
                href = "href1",
                identifier = "id1",
                mediaType = MediaType(string = "application/epub+zip")!!,
                cover = null,
            ),
        )
        val onBookClick: (Long) -> Unit = mockk(relaxed = true)

        composeTestRule.setContent {
            BooksGrid(
                books = books,
                onBookClick = onBookClick,
                onMenuClick = {},
            )
        }

        composeTestRule.onNodeWithText(text = "Book 1").performClick()

        verify { onBookClick(1L) }
    }
}
