package com.example.readiumandroidtestapp.features.bookshelf.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.readiumandroidtestapp.app.AppViewModel
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.ui.theme.AppTheme
import com.example.readiumandroidtestapp.features.bookshelf.BookshelfUiState
import com.example.readiumandroidtestapp.features.bookshelf.BookshelfViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(qualifiers = "w1080dp-h1920dp")
class BookshelfScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val bookshelfViewModel: BookshelfViewModel = mockk(relaxed = true)
    private val appViewModel: AppViewModel = mockk(relaxed = true)

    @Test
    fun `shows loading state`() {
        every { bookshelfViewModel.uiState } returns MutableStateFlow(value = BookshelfUiState.Loading)
        every { appViewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        every { appViewModel.userMessages } returns flowOf()

        composeTestRule.setContent {
            BookshelfScreen(
                onOpenBook = {},
                viewModel = bookshelfViewModel,
                appViewModel = appViewModel,
            )
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun `shows empty state`() {
        every { bookshelfViewModel.uiState } returns MutableStateFlow(value = BookshelfUiState.Empty)
        every { appViewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        every { appViewModel.userMessages } returns flowOf()

        composeTestRule.setContent {
            BookshelfScreen(
                onOpenBook = {},
                viewModel = bookshelfViewModel,
                appViewModel = appViewModel,
            )
        }

        composeTestRule.onNodeWithText(text = "No books yet. Use + to add").assertIsDisplayed()
    }

    @Test
    fun `shows error state`() {
        every { bookshelfViewModel.uiState } returns MutableStateFlow(value = BookshelfUiState.Error)
        every { appViewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        every { appViewModel.userMessages } returns flowOf()

        composeTestRule.setContent {
            BookshelfScreen(
                onOpenBook = {},
                viewModel = bookshelfViewModel,
                appViewModel = appViewModel,
            )
        }

        composeTestRule.onNodeWithText(text = "There wan an error loading the bookshelf. Please try again.")
            .assertIsDisplayed()
    }

    @Test
    fun `shows success state with books`() {
        val books = listOf(
            Book(
                id = 1,
                title = "Test Book",
                href = "href",
                identifier = "id",
                mediaType = MediaType(string = "application/epub+zip")!!,
                cover = null,
            ),
        )
        every { bookshelfViewModel.uiState } returns MutableStateFlow(
            value = BookshelfUiState.Success(
                books = books,
            ),
        )
        every { appViewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        every { appViewModel.userMessages } returns flowOf()

        composeTestRule.setContent {
            BookshelfScreen(
                onOpenBook = {},
                viewModel = bookshelfViewModel,
                appViewModel = appViewModel,
            )
        }

        composeTestRule.onNodeWithText(text = "Test Book").assertIsDisplayed()
    }
}
