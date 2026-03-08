package com.example.readiumandroidtestapp.features.catalogs.ui.publication

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Manifest
import org.readium.r2.shared.util.mediatype.MediaType

@RunWith(AndroidJUnit4::class)
class PublicationDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows error when manifest is null`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val errorText = context.getString(R.string.publication_detail_error)

        composeTestRule.setContent {
            PublicationDetailContent(
                manifest = null,
                onNavigateBack = {},
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = errorText).assertIsDisplayed()
    }

    @Test
    fun `shows error when json is invalid`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val errorText = context.getString(R.string.publication_detail_error)

        composeTestRule.setContent {
            PublicationDetailScreen(
                manifestJson = "{ invalid_json }",
                onNavigateBack = {},
                mainViewModel = mockk(relaxed = true),
            )
        }

        composeTestRule.onNodeWithText(text = errorText).assertIsDisplayed()
    }

    @Test
    fun `shows download button`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val downloadText = context.getString(R.string.download)

        val manifest = mockk<Manifest>(relaxed = true) {
            every { subcollections } returns emptyMap()
            every { links } returns emptyList()
        }

        composeTestRule.setContent {
            PublicationDetailContent(
                manifest = manifest,
                onNavigateBack = {},
                onImportBook = {},
            )
        }

        composeTestRule.onNodeWithText(text = downloadText).assertIsDisplayed()
    }

    @Test
    fun `clicking download calls importBook with correct url`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val downloadText = context.getString(R.string.download)
        val downloadUrl = "http://example.com/book.epub"

        val link = mockk<Link>(relaxed = true) {
            every { rels } returns setOf("http://opds-spec.org/acquisition")
            every { href.toString() } returns downloadUrl
            every { mediaType } returns MediaType(string = "application/epub+zip")
        }

        val manifest = mockk<Manifest>(relaxed = true) {
            every { subcollections } returns emptyMap()
            every { links } returns listOf(link)
        }

        var importedUrl: String? = null

        composeTestRule.setContent {
            PublicationDetailContent(
                manifest = manifest,
                onNavigateBack = {},
                onImportBook = { importedUrl = it },
            )
        }

        composeTestRule.onNodeWithText(text = downloadText).performClick()

        assertTrue(importedUrl == downloadUrl)
    }
}
