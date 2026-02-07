package com.example.readiumandroidtestapp.features.catalogs.ui.feed

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CatalogFeedScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows loading state`() {
        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Loading,
                onCatalogClick = {},
                onAddCatalog = { _, _ -> },
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = {},
            )
        }

        composeTestRule.waitForIdle()
    }

    @Test
    fun `shows error state`() {
        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Error,
                onCatalogClick = {},
                onAddCatalog = { _, _ -> },
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = {},
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val errorText = context.getString(R.string.error_no_feed_data)
        composeTestRule.onNodeWithText(text = errorText).assertIsDisplayed()
    }

    @Test
    fun `shows empty state`() {
        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = emptyList()),
                onCatalogClick = {},
                onAddCatalog = { _, _ -> },
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = {},
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val emptyText = context.getString(R.string.no_catalogs_found)
        composeTestRule.onNodeWithText(text = emptyText).assertIsDisplayed()
    }

    @Test
    fun `shows catalogs and handles click`() {
        val catalog = Catalog(id = 1, title = "Test Catalog", href = "http://example.com", type = 1)
        val onCatalogClick = mockk<(Catalog) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = listOf(catalog)),
                onCatalogClick = onCatalogClick,
                onAddCatalog = { _, _ -> },
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = {},
            )
        }

        composeTestRule.onNodeWithText(text = "Test Catalog").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Test Catalog").performClick()

        verify { onCatalogClick(catalog) }
    }

    @Test
    fun `handles add catalog`() {
        val onAddCatalog = mockk<(String, String) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = emptyList()),
                onCatalogClick = {},
                onAddCatalog = onAddCatalog,
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = {},
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val addText = context.getString(R.string.add_feed)
        val confirmText = context.getString(R.string.add)
        val titleLabel = context.getString(R.string.feed_name)
        val urlLabel = context.getString(R.string.feed_url)

        composeTestRule.onNodeWithText(text = addText, useUnmergedTree = true).performClick()

        composeTestRule.onNodeWithText(text = titleLabel).performTextInput(text = "New Catalog")
        composeTestRule.onNodeWithText(text = urlLabel)
            .performTextInput(text = "http://example.com")

        composeTestRule.onNodeWithText(text = confirmText).performClick()

        verify { onAddCatalog("New Catalog", "http://example.com") }
    }

    @Test
    fun `handles delete catalog`() {
        val catalog = Catalog(id = 1, title = "Test Catalog", href = "http://example.com", type = 1)
        val onDeleteCatalog = mockk<(Catalog) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = listOf(catalog)),
                onCatalogClick = {},
                onAddCatalog = { _, _ -> },
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = onDeleteCatalog,
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val deleteFeedDesc = context.getString(R.string.delete_feed)
        val deleteButtonText = context.getString(R.string.delete_feed)

        // Click delete icon directly
        composeTestRule.onNodeWithContentDescription(label = deleteFeedDesc).performClick()

        composeTestRule.onNode(matcher = hasText(text = deleteButtonText).and(other = hasClickAction()))
            .performClick()

        verify { onDeleteCatalog(catalog) }
    }

    @Test
    fun `handles edit catalog`() {
        val catalog = Catalog(id = 1, title = "Test Catalog", href = "http://example.com", type = 1)
        val onEditCatalog = mockk<(Catalog, String) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = listOf(catalog)),
                onCatalogClick = {},
                onAddCatalog = { _, _ -> },
                onEditCatalog = onEditCatalog,
                onDeleteCatalog = {},
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val editFeedDesc = context.getString(R.string.edit_feed)
        val saveText = context.getString(R.string.save)
        val titleLabel = context.getString(R.string.feed_name)

        // Click edit icon
        composeTestRule.onNodeWithContentDescription(label = editFeedDesc).performClick()

        composeTestRule.onNodeWithText(text = titleLabel).performTextClearance()
        composeTestRule.onNodeWithText(text = titleLabel)
            .performTextInput(text = "Test Catalog Updated")

        // Click save
        composeTestRule.onNodeWithText(text = saveText).performClick()

        verify { onEditCatalog(catalog, "Test Catalog Updated") }
    }

    @Test
    fun `handles add catalog with validation`() {
        val onAddCatalog = mockk<(String, String) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            CatalogFeedContent(
                feedUiState = CatalogFeedUiState.Success(catalogs = emptyList()),
                onCatalogClick = {},
                onAddCatalog = onAddCatalog,
                onEditCatalog = { _, _ -> },
                onDeleteCatalog = {},
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val addText = context.getString(R.string.add_feed)
        val confirmText = context.getString(R.string.add)
        val titleLabel = context.getString(R.string.feed_name)
        val urlLabel = context.getString(R.string.feed_url)

        // 1. Open Dialog
        composeTestRule.onNodeWithText(text = addText, useUnmergedTree = true).performClick()

        // 2. Verify Button is Disabled Initially
        composeTestRule.onNodeWithText(text = confirmText).assertIsNotEnabled()

        // 3. Enter Invalid Data & Verify Disabled
        composeTestRule.onNodeWithText(text = titleLabel).performTextInput(text = "New Catalog")
        composeTestRule.onNodeWithText(text = urlLabel).performTextInput(text = "invalid-url")
        composeTestRule.onNodeWithText(text = confirmText).assertIsNotEnabled()

        // 4. Enter Valid Data & Verify Enabled/Submit
        composeTestRule.onNodeWithText(text = urlLabel).performTextClearance()
        composeTestRule.onNodeWithText(text = urlLabel)
            .performTextInput(text = "http://example.com")

        composeTestRule.onNodeWithText(text = confirmText).performClick()

        verify { onAddCatalog("New Catalog", "http://example.com") }
    }
}
