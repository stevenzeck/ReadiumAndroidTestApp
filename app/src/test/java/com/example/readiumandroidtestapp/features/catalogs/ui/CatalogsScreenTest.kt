package com.example.readiumandroidtestapp.features.catalogs.ui

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.catalogs.ui.feed.CatalogFeedContent
import com.example.readiumandroidtestapp.features.catalogs.ui.feed.CatalogFeedUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CatalogsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `catalogs screen displays feed content`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val catalogsTitle = context.getString(R.string.catalogs)

        composeTestRule.setContent {
            CatalogsScreen(
                listPaneContent = { onCatalogClick ->
                    CatalogFeedContent(
                        feedUiState = CatalogFeedUiState.Loading,
                        onCatalogClick = onCatalogClick,
                        onAddCatalog = { _, _ -> },
                        onEditCatalog = { _, _ -> },
                        onDeleteCatalog = {},
                    )
                },
            )
        }

        composeTestRule.onNodeWithText(catalogsTitle).assertIsDisplayed()
    }
}
