package com.example.readiumandroidtestapp.features.reader.ui.screens

import android.content.Context
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderError
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.publication.Publication

@RunWith(AndroidJUnit4::class)
class ReaderScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val viewModel = mockk<ReaderViewModel>(relaxed = true)

    private fun setupViewModel(uiState: ReaderUiState) {
        every { viewModel.uiState } returns MutableStateFlow(value = uiState)
        every { viewModel.settingsSheetState } returns MutableStateFlow(value = null)
        every { viewModel.bookmarks } returns flowOf(value = emptyList())
        every { viewModel.highlights } returns flowOf(value = emptyList())
        every { viewModel.searchResults } returns flowOf(value = PagingData.empty())
        every { viewModel.searchQuery } returns MutableStateFlow(value = null)
        every { viewModel.isTtsActive } returns MutableStateFlow(value = false)
        every { viewModel.ttsPlayback } returns flowOf(value = false)
        every { viewModel.showHighlightDialog } returns MutableStateFlow(value = false)
    }

    @Test
    fun showsLoadingIndicator_whenStateIsLoading() {
        setupViewModel(ReaderUiState.Loading)

        composeTestRule.setContent {
            ReaderScreen(
                bookId = 1L,
                viewModel = viewModel,
                onNavigateBack = {},
            )
        }

        composeTestRule.onNode(matcher = hasProgressBarRangeInfo(rangeInfo = ProgressBarRangeInfo.Indeterminate))
            .assertExists()
    }

    @Test
    fun showsErrorScreen_whenStateIsError() {
        val error = ReaderError.InvalidBookLocation
        setupViewModel(ReaderUiState.Error(error))

        composeTestRule.setContent {
            ReaderScreen(
                bookId = 1L,
                viewModel = viewModel,
                onNavigateBack = {},
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val expectedMessage = context.getString(R.string.invalid_book_location)
        composeTestRule.onNodeWithText(text = expectedMessage).assertIsDisplayed()
    }

    @Test
    fun showsUnsupportedFormat_whenStateIsVisualAndProfileMatchesNothing() {
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = any()) } returns false
            every { metadata.title } returns "Test Book"
        }
        val book = mockk<Book>(relaxed = true)
        val uiState = ReaderUiState.Visual(
            publication = publication,
            book = book,
            initialLocator = null,
            pdfiumDocumentFactory = mockk(),
            capabilities = ReaderCapabilities(
                isSearchable = false,
                canSpeak = false,
                hasPreferences = false,
            ),
            initialPreferences = mockk<Configurable.Preferences<*>>(relaxed = true),
        )

        setupViewModel(uiState)

        composeTestRule.setContent {
            ReaderScreen(
                bookId = 1L,
                viewModel = viewModel,
                onNavigateBack = {},
            )
        }

        composeTestRule.onNodeWithText(text = "Unsupported format").assertIsDisplayed()
    }
}
