package com.example.readiumandroidtestapp.core.designsystem.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SingleTextInputDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `displays title and message when provided`() {
        val title = "Test Title"
        val message = "Test Message"

        composeTestRule.setContent {
            SingleTextInputDialog(
                title = title,
                message = message,
                label = "Label",
                confirmText = "Confirm",
                onConfirm = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText(text = title).assertIsDisplayed()
        composeTestRule.onNodeWithText(text = message).assertIsDisplayed()
    }

    @Test
    fun `does not display message when null`() {
        val title = "Test Title"

        composeTestRule.setContent {
            SingleTextInputDialog(
                title = title,
                message = null,
                label = "Label",
                confirmText = "Confirm",
                onConfirm = {},
                onDismiss = {},
            )
        }

        composeTestRule.onNodeWithText(text = title).assertIsDisplayed()
    }
}
