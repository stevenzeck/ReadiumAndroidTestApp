package com.example.readiumandroidtestapp.features.reader.ui.visual

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.adapter.pdfium.navigator.PdfiumSettings
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PdfReaderTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FragmentActivity>()

    class TestFragment : Fragment(), VisualNavigator by mockk(relaxed = true) {
        override val currentLocator: StateFlow<Locator> = MutableStateFlow(mockk(relaxed = true))
    }

    @Test
    fun pdfReader_rendersWithoutCrash() {
        val publication = mockk<Publication>(relaxed = true)
        val preferences = PdfiumPreferences()
        val fragmentFactory = mockk<FragmentFactory>(relaxed = true)
        val testFragment = TestFragment()
        val pdfNavigatorFactory =
            mockk<PdfNavigatorFactory<PdfiumSettings, PdfiumPreferences, PdfiumPreferencesEditor>>()

        every {
            pdfNavigatorFactory.createFragmentFactory(
                initialLocator = any(),
                initialPreferences = any(),
            )
        } returns fragmentFactory

        every {
            fragmentFactory.instantiate(
                any(),
                any(),
            )
        } returns testFragment

        composeTestRule.setContent {
            PdfReader(
                publication = publication,
                initialLocator = null,
                initialPreferences = preferences,
                onLocatorChanged = {},
                pdfNavigatorFactory = pdfNavigatorFactory,
                onTap = {},
                onNavigatorReady = {},
            )
        }

        composeTestRule.onNodeWithTag(testTag = "PdfNavigatorHost").assertIsDisplayed()

        verify {
            pdfNavigatorFactory.createFragmentFactory(
                initialLocator = null,
                initialPreferences = preferences,
            )
        }
    }
}
