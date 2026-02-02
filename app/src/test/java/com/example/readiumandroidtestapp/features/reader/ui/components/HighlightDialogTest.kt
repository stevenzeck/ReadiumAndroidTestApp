package com.example.readiumandroidtestapp.features.reader.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HighlightDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun highlightDialog_displaysTitleAndInput() {
        composeTestRule.setContent {
            HighlightDialogContent(
                onDismiss = {},
                onSave = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText(text = "Add Note").assertIsDisplayed()
    }

    @Test
    fun highlightDialog_invokesOnSave_withNoteAndColor() {
        val onSave = mockk<(String, Int) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            HighlightDialogContent(
                onDismiss = {},
                onSave = onSave,
            )
        }

        val noteText = "My Highlight Note"
        composeTestRule.onNodeWithText(text = "Add Note").performTextInput(noteText)

        // Default color is Yellow (0xFFFFFF00)
        val defaultColor = Color(color = 0xFFFFFF00).toArgb()

        composeTestRule.onNodeWithText(text = "Save").performClick()

        verify { onSave(noteText, defaultColor) }
    }

    @Test
    fun highlightDialog_invokesOnDismiss_whenCancelClicked() {
        val onDismiss = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            HighlightDialogContent(
                onDismiss = onDismiss,
                onSave = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText(text = "Cancel").performClick()

        verify { onDismiss() }
    }
}
