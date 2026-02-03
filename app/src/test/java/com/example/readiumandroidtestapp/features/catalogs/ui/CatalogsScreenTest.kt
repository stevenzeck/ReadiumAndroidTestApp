package com.example.readiumandroidtestapp.features.catalogs.ui

import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val dummyCatalog = Catalog(
        id = 1L,
        title = "Test Catalog",
        href = "http://example.com/feed",
        type = 1,
    )

    @Test
    fun shows_feed_screen_initially() {
        composeTestRule.setContent {
            CatalogsScreen(
                feedScreen = {
                    Text(text = "Feed Screen", modifier = Modifier.testTag(tag = "feed"))
                },
                detailScreen = { _, _, _, _, _ ->
                    Text(text = "Detail Screen", modifier = Modifier.testTag(tag = "detail"))
                },
            )
        }

        composeTestRule.onNodeWithTag(testTag = "feed").assertIsDisplayed()
        composeTestRule.onNodeWithTag(testTag = "detail").assertDoesNotExist()
    }

    @Test
    fun navigates_to_detail_screen_on_catalog_click() {
        composeTestRule.setContent {
            CatalogsScreen(
                feedScreen = { onCatalogClick ->
                    Text(
                        text = "Click Me",
                        modifier = Modifier
                            .testTag(tag = "feed_item")
                            .clickable { onCatalogClick(dummyCatalog) },
                    )
                },
                detailScreen = { catalog, _, _, _, _ ->
                    Text(
                        text = "Detail: ${catalog.title}",
                        modifier = Modifier.testTag(tag = "detail"),
                    )
                },
            )
        }

        composeTestRule.onNodeWithTag(testTag = "feed_item").performClick()
        composeTestRule.onNodeWithTag(testTag = "detail").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Detail: Test Catalog").assertIsDisplayed()
    }

    @Test
    fun navigates_back_from_detail_screen() {
        composeTestRule.setContent {
            CatalogsScreen(
                feedScreen = { onCatalogClick ->
                    Text(
                        text = "Feed",
                        modifier = Modifier
                            .testTag("feed")
                            .clickable { onCatalogClick(dummyCatalog) },
                    )
                },
                detailScreen = { _, onNavigateBack, _, _, _ ->
                    Text(
                        text = "Back",
                        modifier = Modifier
                            .testTag("back_button")
                            .clickable { onNavigateBack() },
                    )
                },
            )
        }

        composeTestRule.onNodeWithTag(testTag = "feed").performClick()
        composeTestRule.onNodeWithTag(testTag = "back_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag(testTag = "back_button").performClick()

        composeTestRule.onNodeWithTag(testTag = "feed").assertIsDisplayed()
        composeTestRule.onNodeWithTag(testTag = "back_button").assertDoesNotExist()
    }
}
