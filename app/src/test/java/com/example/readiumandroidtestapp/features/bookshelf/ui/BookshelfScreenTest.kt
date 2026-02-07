package com.example.readiumandroidtestapp.features.bookshelf.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.main.MainViewModel
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(qualifiers = "w1080dp-h1920dp")
class BookshelfScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val bookshelfViewModel: BookshelfViewModel = mockk(relaxed = true)
    private val mainViewModel: MainViewModel = mockk(relaxed = true)

    @Test
    fun `shows loading state`() {
        every { bookshelfViewModel.uiState } returns MutableStateFlow(value = BookshelfUiState.Loading)
        every { mainViewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        every { mainViewModel.userMessages } returns flowOf()

        composeTestRule.setContent {
            BookshelfScreen(
                onOpenBook = {},
                viewModel = bookshelfViewModel,
                mainViewModel = mainViewModel,
            )
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun `shows empty state`() {
        every { bookshelfViewModel.uiState } returns MutableStateFlow(value = BookshelfUiState.Empty)
        every { mainViewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        every { mainViewModel.userMessages } returns flowOf()

        composeTestRule.setContent {
            BookshelfScreen(
                onOpenBook = {},
                viewModel = bookshelfViewModel,
                mainViewModel = mainViewModel,
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val emptyText = context.getString(R.string.empty_bookshelf)
        composeTestRule.onNodeWithText(text = emptyText).assertIsDisplayed()
    }

    @Test
    fun `shows error state`() {
        every { bookshelfViewModel.uiState } returns MutableStateFlow(value = BookshelfUiState.Error)
        every { mainViewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        every { mainViewModel.userMessages } returns flowOf()

        composeTestRule.setContent {
            BookshelfScreen(
                onOpenBook = {},
                viewModel = bookshelfViewModel,
                mainViewModel = mainViewModel,
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val errorText = context.getString(R.string.bookshelf_error)
        composeTestRule.onNodeWithText(text = errorText).assertIsDisplayed()
    }

    @Test
    fun `shows success state with books and handles interactions`() {
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
        every { mainViewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        every { mainViewModel.userMessages } returns flowOf()

        composeTestRule.setContent {
            BookshelfScreen(
                onOpenBook = {},
                viewModel = bookshelfViewModel,
                mainViewModel = mainViewModel,
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()

        composeTestRule.onNodeWithText(text = "Test Book").assertIsDisplayed()

        val moreOptionsDesc = context.getString(R.string.more_options)
        composeTestRule.onAllNodesWithContentDescription(label = moreOptionsDesc).onFirst()
            .performClick()

        val deleteText = context.getString(R.string.delete)
        composeTestRule.onNodeWithText(text = deleteText).performClick()

        val deleteTitle = context.getString(R.string.delete_book_title)
        composeTestRule.onNodeWithText(text = deleteTitle).assertIsDisplayed()

        composeTestRule.onNodeWithText(text = deleteText).performClick()

        verify { bookshelfViewModel.deleteBook(bookId = 1) }
    }

    @Test
    fun `handles FAB import from URL`() {
        every { bookshelfViewModel.uiState } returns MutableStateFlow(value = BookshelfUiState.Empty)
        every { mainViewModel.appTheme } returns MutableStateFlow(value = AppTheme.SYSTEM)
        every { mainViewModel.userMessages } returns flowOf()

        composeTestRule.setContent {
            BookshelfScreen(
                onOpenBook = {},
                viewModel = bookshelfViewModel,
                mainViewModel = mainViewModel,
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val toggleFabDesc = context.getString(R.string.toggle_import_menu)
        val importUrlText = context.getString(R.string.import_from_url)

        // Expand FAB
        composeTestRule.onNodeWithContentDescription(label = toggleFabDesc).performClick()

        composeTestRule.onNodeWithText(text = importUrlText).performClick()

        val urlTitle = context.getString(R.string.enter_url_title)
        composeTestRule.onNodeWithText(text = urlTitle).assertIsDisplayed()

        val urlLabel = context.getString(R.string.url)
        composeTestRule.onNodeWithText(text = urlLabel)
            .performTextInput(text = "http://example.com/book.epub")

        val importAction = context.getString(R.string.import_action)
        composeTestRule.onNodeWithText(text = importAction).performClick()

        verify { mainViewModel.importBook(url = "http://example.com/book.epub") }
    }

    @Test
    fun `tapping scrim collapses FAB`() {
        val mockBookshelfVM = mockk<BookshelfViewModel>(relaxed = true)
        val mockMainVM = mockk<MainViewModel>(relaxed = true)

        every { mockBookshelfVM.uiState } returns MutableStateFlow(BookshelfUiState.Success(books = emptyList()))

        composeTestRule.setContent {
            BookshelfScreen(
                onOpenBook = {},
                viewModel = mockBookshelfVM,
                mainViewModel = mockMainVM,
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()

        // 1. Click FAB to expand it
        composeTestRule.onNodeWithContentDescription(label = context.getString(R.string.toggle_import_menu))
            .performClick()

        // 2. Verify Scrim exists and Click it
        composeTestRule.onNodeWithTag(testTag = "fab_scrim").assertIsDisplayed().performClick()

        // 3. Verify Scrim is gone (FAB collapsed)
        composeTestRule.onNodeWithTag(testTag = "fab_scrim").assertDoesNotExist()
    }
}
