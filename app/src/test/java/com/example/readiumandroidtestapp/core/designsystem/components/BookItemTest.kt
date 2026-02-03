package com.example.readiumandroidtestapp.core.designsystem.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BookItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun bookItem_displaysTitle() {
        composeTestRule.setContent {
            BookItem(
                title = "Test Book",
                coverModel = null,
                onClick = {},
            )
        }

        composeTestRule.onNodeWithText(text = "Test Book").assertIsDisplayed()
    }

    @Test
    fun bookItem_performsClick() {
        var clicked = false
        composeTestRule.setContent {
            BookItem(
                title = "Click Me",
                coverModel = null,
                onClick = { clicked = true },
            )
        }

        composeTestRule.onNodeWithText(text = "Click Me").performClick()
        assertTrue(clicked)
    }
}
