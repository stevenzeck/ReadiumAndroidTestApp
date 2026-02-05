package com.example.readiumandroidtestapp.features.catalogs.ui.detail

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.opds.Feed
import org.readium.r2.shared.opds.Group
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Url
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class CatalogDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val catalog = Catalog(id = 1, title = "Catalog", href = "href", type = 1)
    private val context: Context = ApplicationProvider.getApplicationContext()

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
                onImportBook = {},
            )
        }

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
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = errorMessage).assertIsDisplayed()
    }

    @Test
    fun `shows publications section when feed has publications`() {
        val publicationsTitle = context.getString(R.string.publications)

        val publication = mockk<Publication>(relaxed = true) {
            every { subcollections } returns emptyMap()
            every { metadata.title } returns "Test Book"
            every { links } returns emptyList()
        }

        val feed = mockk<Feed>(relaxed = true) {
            every { publications } returns listOf(publication)
            every { navigation } returns emptyList()
            every { groups } returns emptyList()
        }

        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Success(feed = feed),
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = {},
                onPublicationClick = {},
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = publicationsTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Test Book").assertIsDisplayed()
    }

    @Test
    fun `shows navigation section when feed has navigation links`() {
        val navigationTitle = context.getString(R.string.navigation)
        val linkTitle = "Sub Feed"
        val link = Link(href = Url(url = "sub")!!, title = linkTitle)

        val feed = mockk<Feed>(relaxed = true) {
            every { publications } returns emptyList()
            every { navigation } returns listOf(link)
            every { groups } returns emptyList()
        }

        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Success(feed = feed),
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = {},
                onPublicationClick = {},
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = navigationTitle).assertIsDisplayed()
        composeTestRule.onNodeWithText(text = linkTitle).assertIsDisplayed()
    }

    @Test
    fun `shows groups section when feed has groups`() {
        val groupTitle = "Group Title"
        val group = mockk<Group>(relaxed = true) {
            every { metadata.title } returns groupTitle
            every { links } returns emptyList()
            every { navigation } returns emptyList()
            every { publications } returns emptyList()
        }

        val feed = mockk<Feed>(relaxed = true) {
            every { publications } returns emptyList()
            every { navigation } returns emptyList()
            every { groups } returns listOf(group)
        }

        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Success(feed = feed),
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = {},
                onPublicationClick = {},
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = groupTitle).assertIsDisplayed()
    }

    @Test
    fun `invokes onSubFeedClick when navigation link is clicked`() {
        val linkTitle = "Sub Feed"
        val link = Link(href = Url(url = "sub")!!, title = linkTitle)
        val onSubFeedClick = mockk<(Catalog) -> Unit>(relaxed = true)

        val feed = mockk<Feed>(relaxed = true) {
            every { navigation } returns listOf(link)
        }

        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Success(feed = feed),
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = onSubFeedClick,
                onPublicationClick = {},
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = linkTitle).performClick()
        verify { onSubFeedClick(any()) }
    }

    @Test
    fun `invokes onPublicationClick when publication is clicked`() {
        val publicationTitle = "Test Book"
        val publication = mockk<Publication>(relaxed = true) {
            every { metadata.title } returns publicationTitle
            every { subcollections } returns emptyMap()
            every { links } returns emptyList()
        }
        val onPublicationClick = mockk<(Publication) -> Unit>(relaxed = true)

        val feed = mockk<Feed>(relaxed = true) {
            every { publications } returns listOf(publication)
        }

        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Success(feed = feed),
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = {},
                onPublicationClick = onPublicationClick,
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = publicationTitle).performClick()
        verify { onPublicationClick(publication) }
    }

    @Test
    fun `invokes onNavigateBack when back button is clicked`() {
        val onNavigateBack = mockk<() -> Unit>(relaxed = true)

        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Loading,
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = onNavigateBack,
                onSubFeedClick = {},
                onPublicationClick = {},
                onImportBook = {},
            )
        }

        val backDescription = context.getString(R.string.back)
        composeTestRule.onNodeWithContentDescription(label = backDescription).performClick()
        verify { onNavigateBack() }
    }

    @Test
    fun `clicking group header triggers sub feed navigation`() {
        val groupTitle = "Group Title"
        val groupLink = Link(href = Url(url = "group/self")!!)
        val group = mockk<Group>(relaxed = true) {
            every { metadata.title } returns groupTitle
            every { links } returns listOf(groupLink.copy(rels = setOf("self")))
        }
        val feed = mockk<Feed>(relaxed = true) {
            every { groups } returns listOf(group)
            every { navigation } returns emptyList()
            every { publications } returns emptyList()
        }
        val onSubFeedClick = mockk<(Catalog) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Success(feed),
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = onSubFeedClick,
                onPublicationClick = {},
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = groupTitle).performClick()
        verify { onSubFeedClick(match { it.href == "group/self" }) }
    }

    @Test
    fun `group header is not clickable without self link`() {
        val groupTitle = "Group Title"
        val group = mockk<Group>(relaxed = true) {
            every { metadata.title } returns groupTitle
            every { links } returns emptyList()
        }
        val feed = mockk<Feed>(relaxed = true) {
            every { groups } returns listOf(group)
            every { navigation } returns emptyList()
            every { publications } returns emptyList()
        }
        val onSubFeedClick = mockk<(Catalog) -> Unit>(relaxed = true)

        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Success(feed = feed),
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = onSubFeedClick,
                onPublicationClick = {},
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = groupTitle).performClick()
        verify(exactly = 0) { onSubFeedClick(any()) }
    }

    @Test
    fun `renders nested navigation links in group`() {
        val groupTitle = "Group Title"
        val nestedLinkTitle = "Nested Link"
        val nestedLink = Link(href = Url(url = "nested")!!, title = nestedLinkTitle)

        val group = mockk<Group>(relaxed = true) {
            every { metadata.title } returns groupTitle
            every { navigation } returns listOf(nestedLink)
            every { links } returns emptyList()
            every { publications } returns emptyList()
        }
        val feed = mockk<Feed>(relaxed = true) {
            every { groups } returns listOf(group)
            every { navigation } returns emptyList()
            every { publications } returns emptyList()
        }

        composeTestRule.setContent {
            CatalogDetailContent(
                feedState = FeedState.Success(feed = feed),
                catalog = catalog,
                showBackButton = true,
                onNavigateBack = {},
                onSubFeedClick = {},
                onPublicationClick = {},
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = nestedLinkTitle).assertIsDisplayed()
    }

}
