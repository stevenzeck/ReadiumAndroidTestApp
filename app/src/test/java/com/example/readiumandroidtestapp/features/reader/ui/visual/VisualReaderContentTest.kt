package com.example.readiumandroidtestapp.features.reader.ui.visual

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferences
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferencesEditor
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettings
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.pdfium.document.PdfiumDocumentFactory
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.adapter.pdfium.navigator.PdfiumSettings
import org.readium.navigator.common.RenditionState
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferences
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferencesEditor
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication

@RunWith(AndroidJUnit4::class)
class VisualReaderContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FragmentActivity>()

    class TestFragment : Fragment(), VisualNavigator by mockk(relaxed = true) {
        override val currentLocator: StateFlow<Locator> = MutableStateFlow(mockk(relaxed = true))
    }

    private val book = Book(
        id = 1,
        href = "file://test.epub",
        title = "Test Book",
        identifier = "id",
        rawMediaType = "application/epub+zip",
        cover = null,
    )

    private val testMetadata = mockk<Metadata>(relaxed = true) {
        every { title } returns "Test Publication"
    }

    private val pdfiumDocumentFactory = mockk<PdfiumDocumentFactory>()

    @Test
    fun visualReaderContent_displaysUnsupportedFormat_whenProfileIsUnknown() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = any()) } returns false
        every { publication.metadata } returns testMetadata

        val uiState = createUiState(publication = publication)

        setContent(uiState = uiState)

        composeTestRule.onNodeWithText(text = "Unsupported format").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_displaysReflowableRendition_whenPublicationIsEpub() {
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = Publication.Profile.EPUB) } returns true
            every { conformsTo(profile = Publication.Profile.PDF) } returns false
            every { metadata } returns testMetadata
        }

        val uiState = createUiState(
            publication = publication,
            renditionState = mockk(relaxed = true),
        )

        setContent(
            uiState = uiState,
            visualReaderContent = { _, _ ->
                Text(
                    "Reflowable Web Rendition",
                    modifier = Modifier.testTag("ReflowableWebRendition"),
                )
            },
        )

        composeTestRule.onNodeWithTag(testTag = "ReflowableWebRendition").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_displaysPdfNavigator_whenPublicationIsPdf() {
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = Publication.Profile.EPUB) } returns false
            every { conformsTo(profile = Publication.Profile.PDF) } returns true
            every { metadata } returns testMetadata
        }

        val initialPreferences = ReaderPreferences.Pdf(PdfiumPreferences())

        val uiState = createUiState(
            publication = publication,
            initialPreferences = initialPreferences,
        )

        val fragmentFactory = mockk<FragmentFactory>(relaxed = true)
        val testFragment = TestFragment()
        every { fragmentFactory.instantiate(any(), any()) } returns testFragment

        val navigatorFactory =
            mockk<PdfNavigatorFactory<PdfiumSettings, PdfiumPreferences, PdfiumPreferencesEditor>>()
        every { navigatorFactory.createFragmentFactory(any(), any()) } returns fragmentFactory

        setContent(
            uiState = uiState,
            pdfNavigatorFactory = navigatorFactory,
        )

        composeTestRule.onNodeWithTag(testTag = "PdfNavigatorHost").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_showsSettings_whenStateIsNotNull() {
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = Publication.Profile.EPUB) } returns true
            every { conformsTo(profile = Publication.Profile.PDF) } returns false
            every { metadata } returns testMetadata
        }

        val uiState = createUiState(
            publication = publication,
            renditionState = mockk(relaxed = true),
        )

        val preferencesEditor = mockk<ReflowableWebPreferencesEditor>(relaxed = true)
        val settingsState = ReaderSettings.Configurable(
            ReaderPreferencesEditor.ReflowableWeb(preferencesEditor),
        )

        setContent(
            uiState = uiState,
            settingsSheetState = settingsState,
        )

        // Settings sheet should be displayed
        composeTestRule.onNodeWithText("Reset to Defaults").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_showsHighlightDialog_whenStateIsTrue() {
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = Publication.Profile.EPUB) } returns true
            every { conformsTo(profile = Publication.Profile.PDF) } returns false
            every { metadata } returns testMetadata
        }

        val uiState = createUiState(
            publication = publication,
            renditionState = mockk(relaxed = true),
        )

        setContent(
            uiState = uiState,
            showHighlightDialog = true,
        )

        // Highlight dialog should be displayed.
        composeTestRule.onNodeWithText(text = "Add Highlight").assertIsDisplayed()
    }

    private fun createUiState(
        publication: Publication,
        initialPreferences: ReaderPreferences = ReaderPreferences.ReflowableWeb(
            ReflowableWebPreferences(),
        ),
        capabilities: ReaderCapabilities = ReaderCapabilities(
            isSearchable = false,
            canSpeak = false,
            hasPreferences = false,
        ),
        renditionState: RenditionState<*>? = null,
    ): ReaderUiState.Visual {
        return ReaderUiState.Visual(
            publication = publication,
            book = book,
            initialLocator = null,
            pdfiumDocumentFactory = pdfiumDocumentFactory,
            capabilities = capabilities,
            initialPreferences = initialPreferences,
            renditionState = renditionState,
        )
    }

    private fun setContent(
        uiState: ReaderUiState.Visual,
        onNavigateBack: () -> Unit = {},
        onSettingsClick: () -> Unit = {},
        pdfNavigatorFactory: PdfNavigatorFactory<PdfiumSettings, PdfiumPreferences, PdfiumPreferencesEditor>? = null,
        bookmarks: List<Bookmark> = emptyList(),
        settingsSheetState: ReaderSettings? = null,
        showHighlightDialog: Boolean = false,
        visualReaderContent: @Composable (ReaderUiState.Visual, ReaderSettings?) -> Unit = { state, sheet ->
            VisualReaderContent(
                uiState = state,
                settingsSheetState = sheet,
                onSettingsClick = onSettingsClick,
                onSettingsChange = {},
                onSettingsDismiss = {},
                bookmarks = bookmarks,
                highlights = emptyList(),
                searchResults = flowOf(value = PagingData.empty<SearchItem>()).collectAsLazyPagingItems(),
                searchQuery = "",
                isTtsActive = false,
                isPlaying = false,
                showHighlightDialog = showHighlightDialog,
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
                pdfNavigatorFactory = pdfNavigatorFactory,
            )
        },
    ) {
        composeTestRule.setContent {
            visualReaderContent(uiState, settingsSheetState)
        }
    }
}
