package com.example.readiumandroidtestapp.features.reader.ui.audio.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class AudioProgressBarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `audioProgressBar displays correct time labels`() {
        composeTestRule.setContent {
            AudioProgressBar(
                currentOffset = 90.seconds,
                duration = 185.seconds,
                onSeek = {},
            )
        }

        composeTestRule.onNodeWithText(text = "01:30").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "03:05").assertIsDisplayed()
    }
}
