package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.Context
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
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
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@RunWith(AndroidJUnit4::class)
class AudioPlayerTest {

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
    fun audioPlayer_displaysBookInfo() {
        val navigator = createMockNavigator()
        val sheetState = mockk<SheetState>(relaxed = true)
        every { sheetState.currentValue } returns SheetValue.Expanded

        composeTestRule.setContent {
            ExpandableAudioPlayer(
                book = book,
                navigator = navigator,
                sheetState = sheetState,
                onExpand = {},
                onCollapse = {},
            )
        }

        composeTestRule.onAllNodesWithText(text = "Test Book").onFirst().assertIsDisplayed()
        composeTestRule.onAllNodesWithText(text = "Test Author").onFirst().assertIsDisplayed()
    }

    @Test
    fun audioPlayer_displaysControls() {
        val navigator = createMockNavigator()
        val sheetState = mockk<SheetState>(relaxed = true)
        every { sheetState.currentValue } returns SheetValue.Expanded

        composeTestRule.setContent {
            ExpandableAudioPlayer(
                book = book,
                navigator = navigator,
                sheetState = sheetState,
                onExpand = {},
                onCollapse = {},
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
    fun audioPlayer_togglesPlayPause() {
        val navigator = createMockNavigator()
        val sheetState = mockk<SheetState>(relaxed = true)
        every { sheetState.currentValue } returns SheetValue.Expanded

        composeTestRule.setContent {
            ExpandableAudioPlayer(
                book = book,
                navigator = navigator,
                sheetState = sheetState,
                onExpand = {},
                onCollapse = {},
            )
        }

        val play = context.getString(R.string.play)
        composeTestRule.onNodeWithContentDescription(label = play).performClick()

        composeTestRule.waitForIdle()
        verify(timeout = 1000) { navigator.play() }

        val newPlaybackMock = mockk<AudioNavigator.Playback>(relaxed = true).apply {
            every { playWhenReady } returns true
            every { state } returns mockk<MediaNavigator.State.Ready>(relaxed = true)
            every { index } returns 0
            every { offset } returns 0.seconds
            every { buffered } returns 0.seconds
        }
        (navigator.playback as MutableStateFlow).value = newPlaybackMock

        composeTestRule.waitForIdle()

        val pause = context.getString(R.string.pause)
        composeTestRule.onNodeWithContentDescription(label = pause).performClick()

        verify(timeout = 1000) { navigator.pause() }
    }

    @Test
    fun audioPlayer_seeksForwardAndBackward() {
        val navigator = createMockNavigator()
        val sheetState = mockk<SheetState>(relaxed = true)
        every { sheetState.currentValue } returns SheetValue.Expanded

        composeTestRule.setContent {
            ExpandableAudioPlayer(
                book = book,
                navigator = navigator,
                sheetState = sheetState,
                onExpand = {},
                onCollapse = {},
            )
        }

        val rewind = context.getString(R.string.rewind_30)
        composeTestRule.onNodeWithContentDescription(label = rewind).performClick()

        coVerify { navigator.skip(Duration.parse("-30s")) }

        val forward = context.getString(R.string.forward_30)
        composeTestRule.onNodeWithContentDescription(label = forward).performClick()

        coVerify { navigator.skip(Duration.parse("30s")) }
    }

    @Test
    fun audioPlayer_skipsToNextAndPreviousChapter() {
        val navigator = createMockNavigator()
        val sheetState = mockk<SheetState>(relaxed = true)
        every { sheetState.currentValue } returns SheetValue.Expanded

        composeTestRule.setContent {
            ExpandableAudioPlayer(
                book = book,
                navigator = navigator,
                sheetState = sheetState,
                onExpand = {},
                onCollapse = {},
            )
        }

        // In ExpandableAudioPlayer, if index == 0, previous doesn't do anything to avoid looping.
        // Let's change the mocked index to 1 to test skipping previous.
        (navigator.playback as MutableStateFlow).value =
            mockk<AudioNavigator.Playback>(relaxed = true).apply {
                every { playWhenReady } returns false
                every { state } returns mockk<MediaNavigator.State.Ready>(relaxed = true)
                every { index } returns 1
                every { offset } returns 0.seconds
                every { buffered } returns 0.seconds
            }
        composeTestRule.waitForIdle()

        val previous = context.getString(R.string.previous_chapter)
        composeTestRule.onNodeWithContentDescription(label = previous).performClick()

        coVerify(timeout = 1000) { navigator.skipTo(index = 0, offset = Duration.ZERO) }

        // Change index back to 0 to test skip to next
        (navigator.playback as MutableStateFlow).value =
            mockk<AudioNavigator.Playback>(relaxed = true).apply {
                every { playWhenReady } returns false
                every { state } returns mockk<MediaNavigator.State.Ready>(relaxed = true)
                every { index } returns 0
                every { offset } returns 0.seconds
                every { buffered } returns 0.seconds
            }
        composeTestRule.waitForIdle()

        val next = context.getString(R.string.next_chapter)
        composeTestRule.onNodeWithContentDescription(label = next).performClick()

        coVerify(timeout = 1000) { navigator.skipTo(index = 1, offset = Duration.ZERO) }
    }

    private fun createMockNavigator(): AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences> {
        val navigator =
            mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>(relaxed = true)

        val playbackMock = mockk<AudioNavigator.Playback>(relaxed = true).apply {
            every { playWhenReady } returns false
            every { state } returns mockk<MediaNavigator.State.Ready>(relaxed = true)
            every { index } returns 0
            every { offset } returns 0.seconds
            every { buffered } returns 0.seconds
        }

        val playbackFlow = MutableStateFlow(playbackMock)
        every { navigator.playback } returns playbackFlow

        val readingOrder = mockk<AudioNavigator.ReadingOrder>(relaxed = true)
        val itemMock = mockk<AudioNavigator.ReadingOrder.Item>(relaxed = true).apply {
            every { duration } returns 60.seconds
        }
        val items = listOf(itemMock, itemMock)

        every { readingOrder.items } returns items
        every { navigator.readingOrder } returns readingOrder

        return navigator
    }
}
