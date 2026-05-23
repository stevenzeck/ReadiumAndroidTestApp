package com.example.readiumandroidtestapp.features.reader.ui.screens

import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasProgressBarRangeInfo
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.FragmentActivity
import androidx.paging.PagingData
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderError
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferences
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferences
import org.readium.r2.shared.publication.Publication

@RunWith(AndroidJUnit4::class)
class ReaderScreenTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FragmentActivity>()

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
    fun showsVisualContent_whenStateIsVisual() {
        val publication = mockk<Publication>(relaxed = true)
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
            initialPreferences = ReaderPreferences.ReflowableWeb(ReflowableWebPreferences()),
        )

        setupViewModel(uiState)

        composeTestRule.setContent {
            ReaderScreen(
                bookId = 1L,
                viewModel = viewModel,
                onNavigateBack = {},
                visualReaderContent = { _, _ ->
                    Text(text = "Visual Content", modifier = Modifier.testTag(tag = "visual"))
                },
            )
        }

        composeTestRule.onNodeWithTag(testTag = "visual").assertIsDisplayed()
    }

    @Test
    fun showsAudioContent_whenStateIsAudio() {
        val publication = mockk<Publication>(relaxed = true)
        val book = mockk<Book>(relaxed = true)
        val uiState = ReaderUiState.Audio(
            publication = publication,
            book = book,
            navigator = mockk(relaxed = true),
            preferencesEditor = mockk(relaxed = true),
        )

        setupViewModel(uiState)

        composeTestRule.setContent {
            ReaderScreen(
                bookId = 1L,
                viewModel = viewModel,
                onNavigateBack = {},
                audioReaderContent = { _, _ ->
                    Text(text = "Audio Content", modifier = Modifier.testTag(tag = "audio"))
                },
            )
        }

        composeTestRule.onNodeWithTag(testTag = "audio").assertIsDisplayed()
    }

    @Test
    fun showsDefaultVisualReaderContent_whenSlotIsNotOverridden() {
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
            initialPreferences = ReaderPreferences.ReflowableWeb(ReflowableWebPreferences()),
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

    @Test
    fun `invokes retryLoad when retry button is clicked in Error state`() {
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
        val retryLabel = context.getString(R.string.retry)

        composeTestRule.onNodeWithText(text = retryLabel).performClick()
        verify { viewModel.retryLoad() }
    }

    @Test
    fun `shows default AudioReader content when slot is not overridden`() {
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = any()) } returns true
        }
        val book = mockk<Book>(relaxed = true) {
            every { title } returns "Audio Book Title"
        }

        val playback = mockk<AudioNavigator.Playback>(relaxed = true)
        every { playback.index } returns 0
        every { playback.offset } returns kotlin.time.Duration.ZERO
        every { playback.playWhenReady } returns false
        every { playback.state } returns mockk()

        val playbackFlow = MutableStateFlow(playback)
        val navigator = mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>(
            relaxed = true,
        )
        every { navigator.playback } returns playbackFlow
        every { navigator.readingOrder.items } returns emptyList()

        val uiState = ReaderUiState.Audio(
            publication = publication,
            book = book,
            navigator = navigator,
            preferencesEditor = mockk(relaxed = true),
        )

        setupViewModel(uiState)

        composeTestRule.setContent {
            ReaderScreen(
                bookId = 1L,
                viewModel = viewModel,
                onNavigateBack = {},
            )
        }
    }
}
