package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.common.MediaNavigator
import kotlin.time.Duration

@RunWith(AndroidJUnit4::class)
class AudioReaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val book = Book(
        id = 1,
        creation = 0,
        href = "href",
        title = "Test Book",
        author = "Test Author",
        identifier = "id",
        progression = null,
        rawMediaType = "audio/lcp",
        cover = null,
    )

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun audioReader_displaysBookInfo() {
        val navigator = createMockNavigator()

        composeTestRule.setContent {
            AudioReader(
                book = book,
                navigator = navigator,
                settingsSheetState = null,
                onNavigateBack = {},
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
            )
        }

        composeTestRule.onAllNodesWithText(text = "Test Book").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText(text = "Test Author").onFirst().assertIsDisplayed()
    }

    @Test
    fun audioReader_displaysControls() {
        val navigator = createMockNavigator()

        composeTestRule.setContent {
            AudioReader(
                book = book,
                navigator = navigator,
                settingsSheetState = null,
                onNavigateBack = {},
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
            )
        }

        val previous = context.getString(R.string.previous_chapter)
        val next = context.getString(R.string.next_chapter)
        val rewind = context.getString(R.string.rewind_30)
        val forward = context.getString(R.string.forward_30)
        val play = context.getString(R.string.play)

        composeTestRule.onNodeWithContentDescription(label = previous).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(label = next).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(label = rewind).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(label = forward).assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription(label = play).assertIsDisplayed()
    }

    @Test
    fun audioReader_togglesPlayPause() {
        val navigator = createMockNavigator(isPlaying = false)

        composeTestRule.setContent {
            AudioReader(
                book = book,
                navigator = navigator,
                settingsSheetState = null,
                onNavigateBack = {},
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
            )
        }

        val play = context.getString(R.string.play)
        composeTestRule.onNodeWithContentDescription(label = play).performClick()

        verify { navigator.play() }
    }

    @Test
    fun audioReader_pausesWhenPlaying() {
        val navigator = createMockNavigator(isPlaying = true)

        composeTestRule.setContent {
            AudioReader(
                book = book,
                navigator = navigator,
                settingsSheetState = null,
                onNavigateBack = {},
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
            )
        }

        val pause = context.getString(R.string.pause)
        composeTestRule.onNodeWithContentDescription(label = pause).performClick()

        verify { navigator.pause() }
    }

    @Test
    fun audioReader_skipsToPrevious() {
        val navigator = createMockNavigator(index = 1)

        composeTestRule.setContent {
            AudioReader(
                book = book,
                navigator = navigator,
                settingsSheetState = null,
                onNavigateBack = {},
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
            )
        }

        val previous = context.getString(R.string.previous_chapter)
        composeTestRule.onNodeWithContentDescription(label = previous).performClick()

        composeTestRule.waitForIdle()

        coVerify { navigator.skipTo(index = any(), offset = any()) }
    }

    @Test
    fun audioReader_skipsToNext() {
        val navigator = createMockNavigator(index = 0)

        composeTestRule.setContent {
            AudioReader(
                book = book,
                navigator = navigator,
                settingsSheetState = null,
                onNavigateBack = {},
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
            )
        }

        val next = context.getString(R.string.next_chapter)
        composeTestRule.onNodeWithContentDescription(label = next).performClick()

        composeTestRule.waitForIdle()

        coVerify { navigator.skipTo(index = any(), offset = any()) }
    }

    private fun createMockNavigator(
        isPlaying: Boolean = false,
        index: Int = 0,
    ): AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences> {
        val navigator =
            mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>(relaxed = true)

        val playbackState = mockk<AudioNavigator.Playback>(relaxed = true)
        every { playbackState.playWhenReady } returns isPlaying
        every { playbackState.index } returns index
        every { playbackState.offset } returns Duration.ZERO
        every { playbackState.state } returns mockk<MediaNavigator.State.Ready>()

        val playbackFlow = MutableStateFlow(value = playbackState)
        every { navigator.playback } returns playbackFlow
        every { navigator.readingOrder.items } returns emptyList()

        return navigator
    }
}
