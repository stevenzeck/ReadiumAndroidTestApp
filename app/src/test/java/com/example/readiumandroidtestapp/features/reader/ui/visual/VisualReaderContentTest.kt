package com.example.readiumandroidtestapp.features.reader.ui.visual

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentFactory
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.domain.model.Bookmark
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.input.InputListener
import org.readium.r2.navigator.input.TapEvent
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication

@RunWith(AndroidJUnit4::class)
class VisualReaderContentTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<FragmentActivity>()

    class TestFragment : Fragment(), VisualNavigator by mockk(relaxed = true) {
        override val currentLocator: StateFlow<Locator> = MutableStateFlow(mockk(relaxed = true))
        val listeners = mutableListOf<InputListener>()

        override fun addInputListener(listener: InputListener) {
            listeners.add(listener)
        }

        override fun removeInputListener(listener: InputListener) {
            listeners.remove(listener)
        }
    }

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
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = any()) } returns false
        every { publication.metadata } returns metadata

        val uiState = createUiState(publication = publication)

        setContent(uiState = uiState)

        composeTestRule.onNodeWithText(text = "Unsupported format").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_displaysEpubNavigator_whenPublicationIsEpub() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = Publication.Profile.EPUB) } returns true
        every { publication.conformsTo(profile = Publication.Profile.PDF) } returns false
        every { publication.metadata } returns metadata

        val initialPreferences = EpubPreferences()

        val uiState = createUiState(
            publication = publication,
            initialPreferences = initialPreferences,
        )

        val fragmentFactory = mockk<FragmentFactory>(relaxed = true)
        val testFragment = TestFragment()
        every { fragmentFactory.instantiate(any(), any()) } returns testFragment

        val navigatorFactory = mockk<EpubNavigatorFactory>()
        every {
            navigatorFactory.createFragmentFactory(
                initialLocator = any(),
                initialPreferences = any(),
                configuration = any(),
            )
        } returns fragmentFactory

        setContent(
            uiState = uiState,
            epubNavigatorFactory = navigatorFactory,
        )

        composeTestRule.onNodeWithTag(testTag = "EpubNavigatorHost").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_displaysPdfNavigator_whenPublicationIsPdf() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = Publication.Profile.EPUB) } returns false
        every { publication.conformsTo(profile = Publication.Profile.PDF) } returns true
        every { publication.metadata } returns metadata

        val initialPreferences = PdfiumPreferences()

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
    fun visualReaderContent_togglesOverlay_whenTapped() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = Publication.Profile.EPUB) } returns true
        every { publication.conformsTo(profile = Publication.Profile.PDF) } returns false
        every { publication.metadata } returns metadata

        val uiState = createUiState(
            publication = publication,
            initialPreferences = EpubPreferences(),
        )

        val fragmentFactory = mockk<FragmentFactory>(relaxed = true)
        val testFragment = TestFragment()
        every { fragmentFactory.instantiate(any(), any()) } returns testFragment

        val navigatorFactory = mockk<EpubNavigatorFactory>()
        every {
            navigatorFactory.createFragmentFactory(
                initialLocator = any(),
                initialPreferences = any(),
                configuration = any(),
            )
        } returns fragmentFactory

        setContent(
            uiState = uiState,
            epubNavigatorFactory = navigatorFactory,
        )

        // Overlay should be hidden initially
        composeTestRule.onNodeWithContentDescription(label = "Reading Preferences")
            .assertDoesNotExist()

        // Simulate tap
        val tapEvent = mockk<TapEvent>(relaxed = true)
        composeTestRule.runOnIdle {
            if (testFragment.listeners.isEmpty()) {
                throw AssertionError("No listeners registered")
            }
            testFragment.listeners.forEach { it.onTap(tapEvent) }
        }

        composeTestRule.waitForIdle()
        // Wait for animation
        composeTestRule.mainClock.advanceTimeBy(milliseconds = 2000)
        composeTestRule.waitForIdle()

        // Overlay should be visible
        composeTestRule.onNodeWithContentDescription(label = "Back").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_triggersCallbacks_whenSettingsClicked() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = Publication.Profile.EPUB) } returns true
        every { publication.conformsTo(profile = Publication.Profile.PDF) } returns false
        every { publication.metadata } returns metadata

        val uiState = createUiState(
            publication = publication,
            initialPreferences = EpubPreferences(),
            // Ensure hasPreferences is true
            capabilities = ReaderCapabilities(
                isSearchable = false,
                canSpeak = false,
                hasPreferences = true,
            ),
        )

        val fragmentFactory = mockk<FragmentFactory>(relaxed = true)
        val testFragment = TestFragment()
        every { fragmentFactory.instantiate(any(), any()) } returns testFragment

        val navigatorFactory = mockk<EpubNavigatorFactory>()
        every {
            navigatorFactory.createFragmentFactory(
                initialLocator = any(),
                initialPreferences = any(),
                configuration = any(),
            )
        } returns fragmentFactory

        val onSettingsClick = mockk<() -> Unit>(relaxed = true)

        setContent(
            uiState = uiState,
            epubNavigatorFactory = navigatorFactory,
            onSettingsClick = onSettingsClick,
        )

        // Show overlay
        val tapEvent = mockk<TapEvent>(relaxed = true)
        composeTestRule.runOnIdle {
            testFragment.listeners.forEach { it.onTap(tapEvent) }
        }

        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(milliseconds = 2000)
        composeTestRule.waitForIdle()

        // Click settings
        composeTestRule.onNodeWithContentDescription(label = "Reading Preferences").performClick()

        verify { onSettingsClick() }
    }

    @Test
    fun visualReaderContent_showsToc_whenActionClicked() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = Publication.Profile.EPUB) } returns true
        every { publication.conformsTo(profile = Publication.Profile.PDF) } returns false
        every { publication.metadata } returns metadata

        val uiState = createUiState(
            publication = publication,
            initialPreferences = EpubPreferences(),
        )

        val fragmentFactory = mockk<FragmentFactory>(relaxed = true)
        val testFragment = TestFragment()
        every { fragmentFactory.instantiate(any(), any()) } returns testFragment

        val navigatorFactory = mockk<EpubNavigatorFactory>()
        every {
            navigatorFactory.createFragmentFactory(
                initialLocator = any(),
                initialPreferences = any(),
                configuration = any(),
            )
        } returns fragmentFactory

        setContent(
            uiState = uiState,
            epubNavigatorFactory = navigatorFactory,
            bookmarks = listOf(
                Bookmark(
                    id = 1,
                    bookId = 1,
                    resourceIndex = 0,
                    resourceHref = "href",
                    resourceType = "type",
                    resourceTitle = "title",
                    location = "location",
                    locatorText = "",
                ),
            ),
        )

        // Show overlay
        val tapEvent = mockk<TapEvent>(relaxed = true)
        composeTestRule.runOnIdle {
            testFragment.listeners.forEach { it.onTap(tapEvent) }
        }

        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(milliseconds = 2000)
        composeTestRule.waitForIdle()

        // Click TOC
        composeTestRule.onNodeWithContentDescription(label = "Table of Contents").performClick()

        // TOC should be displayed
        composeTestRule.onNodeWithText(text = "Contents").assertIsDisplayed()
        composeTestRule.onNodeWithText(text = "Bookmarks").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_showsSearch_whenActionClicked() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = Publication.Profile.EPUB) } returns true
        every { publication.conformsTo(profile = Publication.Profile.PDF) } returns false
        every { publication.metadata } returns metadata

        val uiState = createUiState(
            publication = publication,
            initialPreferences = EpubPreferences(),
            capabilities = ReaderCapabilities(
                isSearchable = true,
                canSpeak = false,
                hasPreferences = false,
            ),
        )

        val fragmentFactory = mockk<FragmentFactory>(relaxed = true)
        val testFragment = TestFragment()
        every { fragmentFactory.instantiate(any(), any()) } returns testFragment

        val navigatorFactory = mockk<EpubNavigatorFactory>()
        every {
            navigatorFactory.createFragmentFactory(
                initialLocator = any(),
                initialPreferences = any(),
                configuration = any(),
            )
        } returns fragmentFactory

        setContent(
            uiState = uiState,
            epubNavigatorFactory = navigatorFactory,
        )

        // Show overlay
        val tapEvent = mockk<TapEvent>(relaxed = true)
        composeTestRule.runOnIdle {
            testFragment.listeners.forEach { it.onTap(tapEvent) }
        }

        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(milliseconds = 2000)
        composeTestRule.waitForIdle()

        // Click Search
        composeTestRule.onNodeWithContentDescription(label = "Search").performClick()
        composeTestRule.onNodeWithText(text = "Search").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_showsSettings_whenStateIsNotNull() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = Publication.Profile.EPUB) } returns true
        every { publication.conformsTo(profile = Publication.Profile.PDF) } returns false
        every { publication.metadata } returns metadata

        val uiState = createUiState(
            publication = publication,
            initialPreferences = EpubPreferences(),
        )

        val fragmentFactory = mockk<FragmentFactory>(relaxed = true)
        val testFragment = TestFragment()
        every { fragmentFactory.instantiate(any(), any()) } returns testFragment

        val navigatorFactory = mockk<EpubNavigatorFactory>()
        every {
            navigatorFactory.createFragmentFactory(
                initialLocator = any(),
                initialPreferences = any(),
                configuration = any(),
            )
        } returns fragmentFactory

        val preferencesEditor = mockk<PreferencesEditor<EpubPreferences>>(relaxed = true)
        val settingsSheetState = ReaderSettingsSheet.Configurable(preferencesEditor)

        setContent(
            uiState = uiState,
            epubNavigatorFactory = navigatorFactory,
            settingsSheetState = settingsSheetState,
        )

        // Settings sheet should be displayed
        composeTestRule.onNodeWithText("Reset to Defaults").assertIsDisplayed()
    }

    @Test
    fun visualReaderContent_showsAnnotationDialog_whenStateIsTrue() {
        val publication = mockk<Publication>(relaxed = true)
        every { publication.conformsTo(profile = Publication.Profile.EPUB) } returns true
        every { publication.conformsTo(profile = Publication.Profile.PDF) } returns false
        every { publication.metadata } returns metadata

        val uiState = createUiState(
            publication = publication,
            initialPreferences = EpubPreferences(),
        )

        val fragmentFactory = mockk<FragmentFactory>(relaxed = true)
        val testFragment = TestFragment()
        every { fragmentFactory.instantiate(any(), any()) } returns testFragment

        val navigatorFactory = mockk<EpubNavigatorFactory>()
        every {
            navigatorFactory.createFragmentFactory(
                initialLocator = any(),
                initialPreferences = any(),
                configuration = any(),
            )
        } returns fragmentFactory

        setContent(
            uiState = uiState,
            epubNavigatorFactory = navigatorFactory,
            showAnnotationDialog = true,
        )

        // Annotation dialog should be displayed.
        composeTestRule.onNodeWithText(text = "Add Annotation").assertIsDisplayed()
    }

    private fun createUiState(
        publication: Publication,
        initialPreferences: Configurable.Preferences<*> = mockk(),
        capabilities: ReaderCapabilities = ReaderCapabilities(
            isSearchable = false,
            canSpeak = false,
            hasPreferences = false,
        ),
    ): ReaderUiState.Visual {
        return ReaderUiState.Visual(
            publication = publication,
            book = book,
            initialLocator = null,
            pdfiumDocumentFactory = pdfiumDocumentFactory,
            capabilities = capabilities,
            initialPreferences = initialPreferences,
        )
    }

    private fun setContent(
        uiState: ReaderUiState.Visual,
        onNavigateBack: () -> Unit = {},
        onSettingsClick: () -> Unit = {},
        epubNavigatorFactory: EpubNavigatorFactory? = null,
        pdfNavigatorFactory: PdfNavigatorFactory<PdfiumSettings, PdfiumPreferences, PdfiumPreferencesEditor>? = null,
        bookmarks: List<Bookmark> = emptyList(),
        settingsSheetState: ReaderSettingsSheet? = null,
        showAnnotationDialog: Boolean = false,
    ) {
        composeTestRule.setContent {
            val searchResults =
                flowOf(value = PagingData.empty<SearchItem>()).collectAsLazyPagingItems()

            VisualReaderContent(
                uiState = uiState,
                settingsSheetState = settingsSheetState,
                onSettingsClick = onSettingsClick,
                onSettingsChange = {},
                onSettingsDismiss = {},
                bookmarks = bookmarks,
                annotations = emptyList(),
                searchResults = searchResults,
                searchQuery = "",
                isTtsActive = false,
                isPlaying = false,
                showAnnotationDialog = showAnnotationDialog,
                onNavigateBack = onNavigateBack,
                onVisualLocatorChanged = {},
                onNavigatorReady = {},
                onAnnotateAction = {},
                startTts = {},
                stopTts = {},
                play = {},
                pause = {},
                previous = {},
                next = {},
                onSearchQueryChanged = {},
                saveAnnotation = { _, _, _ -> },
                dismissAnnotationDialog = {},
                epubNavigatorFactory = epubNavigatorFactory,
                pdfNavigatorFactory = pdfNavigatorFactory,
            )
        }
    }
}
