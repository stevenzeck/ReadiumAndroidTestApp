package com.example.readiumandroidtestapp.features.catalogs.ui.feed.components

import android.content.Context
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.readiumandroidtestapp.R
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddCatalogDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `add button is disabled initially`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val addButtonText = context.getString(R.string.add)

        composeTestRule.setContent {
            AddCatalogDialog(
                onDismissRequest = {},
                onConfirm = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText(text = addButtonText).assertIsNotEnabled()
    }

    @Test
    fun `add button is enabled when fields are valid`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val addButtonText = context.getString(R.string.add)
        val titleLabel = context.getString(R.string.feed_name)
        val urlLabel = context.getString(R.string.feed_url)

        composeTestRule.setContent {
            AddCatalogDialog(
                onDismissRequest = {},
                onConfirm = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText(text = titleLabel).performTextInput(text = "My Feed")
        composeTestRule.onNodeWithText(text = urlLabel)
            .performTextInput(text = "http://example.com/feed")

        composeTestRule.onNodeWithText(text = addButtonText).assertIsEnabled()
    }

    @Test
    fun `add button is disabled when url is invalid`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val addButtonText = context.getString(R.string.add)
        val titleLabel = context.getString(R.string.feed_name)
        val urlLabel = context.getString(R.string.feed_url)

        composeTestRule.setContent {
            AddCatalogDialog(
                onDismissRequest = {},
                onConfirm = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText(text = titleLabel).performTextInput(text = "My Feed")
        composeTestRule.onNodeWithText(text = urlLabel).performTextInput(text = "invalid-url")

        composeTestRule.onNodeWithText(text = addButtonText).assertIsNotEnabled()
    }

    @Test
    fun `cancel button invokes onDismissRequest`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val cancelText = context.getString(R.string.cancel)
        var dismissed = false

        composeTestRule.setContent {
            AddCatalogDialog(
                onDismissRequest = { dismissed = true },
                onConfirm = { _, _ -> },
            )
        }

        composeTestRule.onNodeWithText(text = cancelText).performClick()
        assertTrue(dismissed)
    }

    @Test
    fun `confirm button invokes onConfirm with input data`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val addButtonText = context.getString(R.string.add)
        val titleLabel = context.getString(R.string.feed_name)
        val urlLabel = context.getString(R.string.feed_url)

        var confirmedTitle = ""
        var confirmedUrl = ""

        composeTestRule.setContent {
            AddCatalogDialog(
                onDismissRequest = {},
                onConfirm = { t, u ->
                    confirmedTitle = t
                    confirmedUrl = u
                },
            )
        }

        composeTestRule.onNodeWithText(text = titleLabel).performTextInput(text = "Test Feed")
        composeTestRule.onNodeWithText(text = urlLabel).performTextInput(text = "https://test.com")
        composeTestRule.onNodeWithText(text = addButtonText).performClick()

        assertTrue(confirmedTitle == "Test Feed")
        assertTrue(confirmedUrl == "https://test.com")
    }
}
