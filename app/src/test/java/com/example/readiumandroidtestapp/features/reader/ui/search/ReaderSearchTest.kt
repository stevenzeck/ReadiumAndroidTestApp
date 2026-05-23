package com.example.readiumandroidtestapp.features.reader.ui.search

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

@RunWith(AndroidJUnit4::class)
class ReaderSearchTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `SearchBottomSheet displays query`() {
        val query = "test query"
        composeTestRule.setContent {
            val results = flowOf(value = PagingData.empty<SearchItem>()).collectAsLazyPagingItems()
            SearchBottomSheet(
                query = query,
                onQueryChange = {},
                results = results,
                onDismissRequest = {},
                onLocatorSelected = {},
            )
        }

        composeTestRule.onNodeWithText(text = query).assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun `SearchBottomSheet displays results`() {
        val locator = Locator(
            href = Url(url = "chap1")!!,
            mediaType = MediaType(string = "text/html")!!,
            text = Locator.Text(highlight = "Found text"),
            title = "Chapter 1",
        )
        val items = listOf(
            SearchItem.Header(title = "Chapter 1"),
            SearchItem.Result(locator = locator),
        )

        composeTestRule.setContent {
            val results = flowOf(value = PagingData.from(data = items)).collectAsLazyPagingItems()
            SearchBottomSheet(
                query = "test",
                onQueryChange = {},
                results = results,
                onDismissRequest = {},
                onLocatorSelected = {},
            )
        }

        composeTestRule.waitUntil(timeoutMillis = 5000L) {
            composeTestRule.onAllNodesWithText("Chapter 1").fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithText(text = "Chapter 1").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Found text").assertIsDisplayed()
    }
}
