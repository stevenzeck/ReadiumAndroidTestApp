package com.example.readiumandroidtestapp.features.reader.ui.visual

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class VisualReaderContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val book = Book(
        id = 1,
        href = "file://test.epub",
        title = "Test Book",
        identifier = "id",
        rawMediaType = "application/epub+zip",
        cover = null,
    )

    private val metadata = mockk<Metadata>(relaxed = true) {
        every { title } returns "Test Publication"
    }

    private val pdfiumDocumentFactory = mockk<PdfiumDocumentFactory>()

    @Test
    fun visualReaderContent_displaysUnsupportedFormat_whenProfileIsUnknown() {
        val publication = mockk<Publication>()
        every { publication.conformsTo(profile = any()) } returns false
        every { publication.metadata } returns metadata

        val uiState = createUiState(publication = publication)

        setContent(uiState = uiState)

        composeTestRule.onNodeWithText(text = "Unsupported format").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_displaysEpubNavigator_whenPublicationIsEpub() {
        val publication = mockk<Publication>()
        every { publication.conformsTo(profile = Publication.Profile.EPUB) } returns true
        every { publication.conformsTo(profile = Publication.Profile.PDF) } returns false
        every { publication.metadata } returns metadata

        val initialPreferences = mockk<EpubPreferences>(relaxed = true)

        val uiState = createUiState(
            publication = publication,
            initialPreferences = initialPreferences as Configurable.Preferences<*>,
        )

        setContent(uiState = uiState)

        composeTestRule.onNodeWithTag(testTag = "EpubNavigatorHost").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_displaysPdfNavigator_whenPublicationIsPdf() {
        val publication = mockk<Publication>()
        every { publication.conformsTo(profile = Publication.Profile.EPUB) } returns false
        every { publication.conformsTo(profile = Publication.Profile.PDF) } returns true
        every { publication.metadata } returns metadata

        val initialPreferences = mockk<PdfiumPreferences>(relaxed = true)

        val uiState = createUiState(
            publication = publication,
            initialPreferences = initialPreferences as Configurable.Preferences<*>,
        )

        setContent(uiState = uiState)

        composeTestRule.onNodeWithTag(testTag = "PdfNavigatorHost").assertIsDisplayed()
    }

    private fun createUiState(
        publication: Publication,
        initialPreferences: Configurable.Preferences<*> = mockk(),
    ): ReaderUiState.Visual {
        return ReaderUiState.Visual(
            publication = publication,
            book = book,
            initialLocator = null,
            pdfiumDocumentFactory = pdfiumDocumentFactory,
            capabilities = ReaderCapabilities(
                isSearchable = false,
                canSpeak = false,
                hasPreferences = false,
            ),
            initialPreferences = initialPreferences,
        )
    }

    private fun setContent(
        uiState: ReaderUiState.Visual,
        onNavigateBack: () -> Unit = {},
    ) {
        composeTestRule.setContent {
            val searchResults =
                flowOf(value = PagingData.empty<SearchItem>()).collectAsLazyPagingItems()

            VisualReaderContent(
                uiState = uiState,
                settingsSheetState = null,
                onSettingsClick = {},
                onSettingsChange = {},
                onSettingsDismiss = {},
                bookmarks = emptyList(),
                highlights = emptyList(),
                searchResults = searchResults,
                searchQuery = "",
                isTtsActive = false,
                isPlaying = false,
                showHighlightDialog = false,
                onNavigateBack = onNavigateBack,
                onVisualLocatorChanged = {},
                onNavigatorReady = {},
                onHighlightAction = {},
                startTts = {},
                stopTts = {},
                play = {},
                pause = {},
                previous = {},
                next = {},
                onSearchQueryChanged = {},
                saveHighlight = { _, _ -> },
                dismissHighlightDialog = {},
            )
        }
    }
}
