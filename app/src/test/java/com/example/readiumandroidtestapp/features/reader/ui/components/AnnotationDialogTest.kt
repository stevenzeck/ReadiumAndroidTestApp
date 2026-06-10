package com.example.readiumandroidtestapp.features.reader.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnnotationDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun annotationDialog_displaysTitleAndInput() {
        composeTestRule.setContent {
            AnnotationDialogContent(
                onDismiss = {},
                onSave = { _, _, _ -> },
            )
        }

        composeTestRule.onNodeWithText(text = "Add Note").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Style").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Highlight").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Underline").assertIsDisplayed()
    }

    @Test
    fun annotationDialog_prepopulatesFields_whenEditing() {
        val annotation = ReaderAnnotation(
            bookId = 1,
            style = ReaderAnnotation.Style.UNDERLINE,
            tint = Color.Red.toArgb(),
            locator = mockk(relaxed = true),
            annotation = "Existing note",
        )

        composeTestRule.setContent {
            AnnotationDialogContent(
                annotation = annotation,
                onDismiss = {},
                onSave = { _, _, _ -> },
            )
        }

        composeTestRule.onNodeWithText(text = "Existing note").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Underline").assertIsDisplayed()
    }

    @Test
    fun annotationDialog_invokesOnSave_withNoteAndColorAndStyle() {
        val onSave = mockk<(String, Int, ReaderAnnotation.Style) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            AnnotationDialogContent(
                onDismiss = {},
                onSave = onSave,
            )
        }

        val noteText = "My Annotation Note"
        composeTestRule.onNodeWithText(text = "Add Note").performTextInput(noteText)

        // Select Underline
        composeTestRule.onNodeWithText(text = "Underline").performClick()

        // Default color is Yellow (0xFFFFFF00)
        val defaultColor = Color(color = 0xFFFFFF00).toArgb()

        composeTestRule.onNodeWithText(text = "Save").performClick()

        verify { onSave(noteText, defaultColor, ReaderAnnotation.Style.UNDERLINE) }
    }

    @Test
    fun annotationDialog_invokesOnDismiss_whenCancelClicked() {
        val onDismiss = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            AnnotationDialogContent(
                onDismiss = onDismiss,
                onSave = { _, _, _ -> },
            )
        }

        composeTestRule.onNodeWithText(text = "Cancel").performClick()

        verify { onDismiss() }
    }
}
