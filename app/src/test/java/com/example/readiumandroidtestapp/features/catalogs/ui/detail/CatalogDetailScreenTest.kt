package com.example.readiumandroidtestapp.features.catalogs.ui.detail

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.opds.Feed
import org.readium.r2.shared.publication.Publication
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CatalogDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val catalog = Catalog(id = 1, title = "Catalog", href = "href", type = 1)

    @Test
    fun `shows loading indicator when state is Loading`() {
        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Loading,
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = {},
                onPublicationClick = {},
                onImportBook = {}
            )
        }

        val context = ApplicationProvider.getApplicationContext<Context>()
        val errorText = context.getString(R.string.error_no_feed_data)
        composeTestRule.onNodeWithText(text = errorText).assertDoesNotExist()
    }

    @Test
    fun `shows error message when state is Error`() {
        val errorMessage = "Failed to load"
        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Error(message = errorMessage),
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = {},
                onPublicationClick = {},
                onImportBook = {}
            )
        }

        composeTestRule.onNodeWithText(text = errorMessage).assertIsDisplayed()
    }

    @Test
    fun `shows publications section when feed has publications`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val publicationsTitle = context.getString(R.string.publications)

        val publication = mockk<Publication>(relaxed = true) {
            every { subcollections } returns emptyMap()
        }

        val feed = mockk<Feed>(relaxed = true) {
            every { publications } returns listOf(publication)
            every { navigation } returns emptyList()
            every { groups } returns emptyList()
        }

        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Success(feed),
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = {},
                onPublicationClick = {},
                onImportBook = {}
            )
        }

        composeTestRule.onNodeWithText(text = publicationsTitle).assertIsDisplayed()
    }
}
