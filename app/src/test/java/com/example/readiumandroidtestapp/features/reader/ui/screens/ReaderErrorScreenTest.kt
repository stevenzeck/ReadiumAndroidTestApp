package com.example.readiumandroidtestapp.features.reader.ui.screens

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderError
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReaderErrorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun displaysErrorMessageAndHandlesRetry() {
        var retryClicked = false
        val error = ReaderError.InvalidBookLocation
        val context = ApplicationProvider.getApplicationContext<Context>()

        composeTestRule.setContent {
            ReaderErrorScreen(
                error = error,
                onRetry = { retryClicked = true },
            )
        }

        // Verify error message is displayed
        val expectedMessage = context.getString(R.string.invalid_book_location)
        composeTestRule.onNodeWithText(text = expectedMessage).assertIsDisplayed()

        // Verify Retry button exists and is clickable
        val retryText = context.getString(R.string.retry)
        composeTestRule.onNodeWithText(text = retryText).performClick()

        assertTrue(retryClicked)
    }

    @Test
    fun displaysFormattedErrorMessage() {
        val msg = "Connection Failed"
        val error = ReaderError.AssetRetrievalFailed(cause = Throwable(msg))
        val context = ApplicationProvider.getApplicationContext<Context>()

        composeTestRule.setContent {
            ReaderErrorScreen(
                error = error,
                onRetry = {},
            )
        }

        val expectedFormat = context.getString(R.string.failed_asset_retrieval, msg)
        composeTestRule.onNodeWithText(text = expectedFormat).assertIsDisplayed()
    }
}
