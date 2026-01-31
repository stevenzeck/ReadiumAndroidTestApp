package com.example.readiumandroidtestapp.features.reader.ui.audio

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.model.Book
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import kotlin.time.Duration

@RunWith(AndroidJUnit4::class)
class AudioReaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun audioReader_displaysBookInfo() {
        val book = Book(
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

        val navigator =
            mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>(relaxed = true)

        val playbackState = mockk<AudioNavigator.Playback>(relaxed = true)
        every { playbackState.playWhenReady } returns false
        every { playbackState.index } returns 0
        every { playbackState.offset } returns Duration.ZERO

        val playbackFlow = MutableStateFlow(value = playbackState)
        every { navigator.playback } returns playbackFlow

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
}
