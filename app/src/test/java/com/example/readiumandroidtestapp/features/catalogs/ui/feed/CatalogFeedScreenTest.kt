package com.example.readiumandroidtestapp.features.catalogs.ui.feed

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onLast
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
class CatalogFeedScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows empty view when list is empty`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val emptyMessage = context.getString(R.string.no_catalogs_found)

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = emptyList()),
                onCatalogClick = {},
                onAddCatalog = { _, _ -> },
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = {},
            )
        }

        composeTestRule.onNodeWithText(text = emptyMessage).assertIsDisplayed()
    }

    @Test
    fun `shows catalogs when list is not empty`() {
        val catalog = Catalog(id = 1, title = "My Feed", href = "http://test.com", type = 1)

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = listOf(catalog)),
                onCatalogClick = {},
                onAddCatalog = { _, _ -> },
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = {},
            )
        }

        composeTestRule.onNodeWithText(text = "My Feed").assertIsDisplayed()
    }

    @Test
    fun `clicking add fab opens dialog`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val addFeedText = context.getString(R.string.add_feed)
        val cancelText = context.getString(R.string.cancel)

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = emptyList()),
                onCatalogClick = {},
                onAddCatalog = { _, _ -> },
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = {},
            )
        }

        composeTestRule.onNodeWithText(addFeedText, useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithText(cancelText).assertIsDisplayed()
    }

    @Test
    fun `invokes onCatalogClick when catalog is clicked`() {
        val catalog = Catalog(id = 1, title = "My Feed", href = "http://test.com", type = 1)
        var clickedCatalog: Catalog? = null

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = listOf(catalog)),
                onCatalogClick = { clickedCatalog = it },
                onAddCatalog = { _, _ -> },
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = {},
            )
        }

        composeTestRule.onNodeWithText(text = "My Feed").performClick()

        assertTrue(clickedCatalog == catalog)
    }

    @Test
    fun `invokes onDeleteCatalog when delete icon is clicked`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val deleteContentDescription = context.getString(R.string.delete_feed)
        val catalog = Catalog(id = 1, title = "My Feed", href = "http://test.com", type = 1)
        var deletedCatalog: Catalog? = null

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = listOf(catalog)),
                onCatalogClick = {},
                onAddCatalog = { _, _ -> },
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = { deletedCatalog = it },
            )
        }

        composeTestRule.onNodeWithContentDescription(
            label = deleteContentDescription,
            useUnmergedTree = true,
        ).performClick()

        composeTestRule.onAllNodesWithText(text = deleteContentDescription).onLast().performClick()

        assertTrue(deletedCatalog == catalog)
    }

    @Test
    fun `invokes onEditCatalog when edit icon is clicked`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val editContentDescription = context.getString(R.string.edit_feed)
        val saveText = context.getString(R.string.save)
        val catalog = Catalog(id = 1, title = "My Feed", href = "http://test.com", type = 1)
        var editedCatalog: Catalog? = null

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = listOf(catalog)),
                onCatalogClick = {},
                onAddCatalog = { _, _ -> },
                onEditCatalog = { catalog, _ -> editedCatalog = catalog },
                onDeleteCatalog = {},
            )
        }

        composeTestRule.onNodeWithContentDescription(
            label = editContentDescription,
            useUnmergedTree = true,
        ).performClick()

        composeTestRule.onNodeWithText(text = saveText).performClick()

        assertTrue(editedCatalog == catalog)
    }
}
