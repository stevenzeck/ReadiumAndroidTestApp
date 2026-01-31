package com.example.readiumandroidtestapp.features.catalogs.ui.publication

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.core.app.ApplicationProvider
import com.example.readiumandroidtestapp.R
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Manifest
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
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
}
