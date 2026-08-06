package com.example.readiumandroidtestapp.features.reader.ui.audio

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.model.Book
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator

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

    @Test
    fun audioPlayer_displaysBookInfo() {
        val navigator = createMockNavigator()
        val sheetState = mockk<SheetState>(relaxed = true)
        every { sheetState.currentValue } returns SheetValue.Expanded
        every { sheetState.targetValue } returns SheetValue.Expanded

        composeTestRule.setContent {
            ExpandableAudioPlayer(
                book = book,
                navigator = navigator,
                editor = null,
                mediaController = null,
                sheetState = sheetState,
                onExpand = {},
                onCollapse = {},
            )
        }

        composeTestRule.onAllNodesWithText(text = "Test Book").onFirst().assertIsDisplayed()
    }

    private fun createMockNavigator(): AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences> {
        return mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>(relaxed = true)
    }
}
