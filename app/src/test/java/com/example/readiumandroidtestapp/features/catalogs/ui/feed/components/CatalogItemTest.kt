package com.example.readiumandroidtestapp.features.catalogs.ui.feed.components

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CatalogItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val catalog = Catalog(
        id = 1,
        title = "Test Catalog",
        href = "http://example.com/feed",
        type = 1,
    )

    @Test
    fun `displays title and href`() {
        composeTestRule.setContent {
            CatalogItem(
                catalog = catalog,
                onClick = {},
                onEdit = {},
                onDelete = {},
            )
        }

        composeTestRule.onNodeWithText(text = "Test Catalog").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "http://example.com/feed").assertIsDisplayed()
    }

    @Test
    fun `clicking item invokes onClick`() {
        var clicked = false
        composeTestRule.setContent {
            CatalogItem(
                catalog = catalog,
                onClick = { clicked = true },
                onEdit = {},
                onDelete = {},
            )
        }

        composeTestRule.onNodeWithText(text = "Test Catalog").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `clicking edit icon invokes onEdit`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val editContentDescription = context.getString(R.string.edit_feed)
        var edited = false

        composeTestRule.setContent {
            CatalogItem(
                catalog = catalog,
                onClick = {},
                onEdit = { edited = true },
                onDelete = {},
            )
        }

        composeTestRule.onNodeWithContentDescription(label = editContentDescription).performClick()
        assertTrue(edited)
    }

    @Test
    fun `clicking delete icon invokes onDelete`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val deleteContentDescription = context.getString(R.string.delete_feed)
        var deleted = false

        composeTestRule.setContent {
            CatalogItem(
                catalog = catalog,
                onClick = {},
                onEdit = {},
                onDelete = { deleted = true },
            )
        }

        composeTestRule.onNodeWithContentDescription(label = deleteContentDescription)
            .performClick()
        assertTrue(deleted)
    }
}
