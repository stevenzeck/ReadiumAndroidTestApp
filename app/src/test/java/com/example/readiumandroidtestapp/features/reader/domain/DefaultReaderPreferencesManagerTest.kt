package com.example.readiumandroidtestapp.features.reader.domain

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.features.reader.data.AndroidTtsNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.BookPreferencesRepository
import com.example.readiumandroidtestapp.features.reader.data.FixedWebPreferencesSerializer
import com.example.readiumandroidtestapp.features.reader.data.PdfNavigatorFactoryWrapper
import com.example.readiumandroidtestapp.features.reader.data.PreferencesSerializerFactory
import com.example.readiumandroidtestapp.features.reader.data.ReflowableWebPreferencesSerializer
import com.example.readiumandroidtestapp.features.reader.ui.audio.AppAudioNavigatorFactory
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderNavigator
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferences
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferencesEditor
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import com.example.readiumandroidtestapp.features.reader.ui.tts.ReaderTtsManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesSerializer
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesSerializer
import org.readium.adapter.pdfium.navigator.PdfiumSettings
import org.readium.navigator.common.ExportableLocation
import org.readium.navigator.common.GoLocation
import org.readium.navigator.common.NavigationController
import org.readium.navigator.common.SettingsController
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesEditor
import org.readium.navigator.media.tts.android.AndroidTtsPreferencesSerializer
import org.readium.navigator.web.fixedlayout.FixedWebRenditionFactory
import org.readium.navigator.web.fixedlayout.preferences.FixedWebPreferences
import org.readium.navigator.web.fixedlayout.preferences.FixedWebSettings
import org.readium.navigator.web.reflowable.ReflowableWebRenditionFactory
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferences
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferencesEditor
import org.readium.navigator.web.reflowable.preferences.ReflowableWebSettings
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.Preference
import org.readium.r2.shared.publication.Layout
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Language
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class DefaultReaderPreferencesManagerTest {

    private val application: Application = mockk()
    private val bookPreferencesRepository: BookPreferencesRepository = mockk(relaxed = true)
    private val audioNavigatorFactory: AppAudioNavigatorFactory = mockk(relaxed = true)
    private val ttsNavigatorFactoryWrapper: AndroidTtsNavigatorFactoryWrapper =
        mockk(relaxed = true)
    private val pdfNavigatorFactoryWrapper: PdfNavigatorFactoryWrapper = mockk(relaxed = true)
    private val preferencesSerializerFactory: PreferencesSerializerFactory = mockk(relaxed = true)
    private val ttsManager: ReaderTtsManager = mockk(relaxed = true)

    private lateinit var manager: DefaultReaderPreferencesManager

    private val reflowableSerializer = mockk<ReflowableWebPreferencesSerializer>(relaxed = true)
    private val fixedSerializer = mockk<FixedWebPreferencesSerializer>(relaxed = true)
    private val pdfSerializer = mockk<PdfiumPreferencesSerializer>(relaxed = true)
    private val ttsSerializer = mockk<AndroidTtsPreferencesSerializer>(relaxed = true)
    private val audioSerializer = mockk<ExoPlayerPreferencesSerializer>(relaxed = true)

    @Before
    fun setUp() {
        mockkObject(ReflowableWebRenditionFactory)
        mockkObject(FixedWebRenditionFactory)

        every { preferencesSerializerFactory.createReflowableWebSerializer() } returns reflowableSerializer
        every { preferencesSerializerFactory.createFixedWebSerializer() } returns fixedSerializer
        every { preferencesSerializerFactory.createPdfiumSerializer() } returns pdfSerializer
        every { preferencesSerializerFactory.createAndroidTtsSerializer() } returns ttsSerializer
        every { preferencesSerializerFactory.createExoPlayerSerializer() } returns audioSerializer

        manager = DefaultReaderPreferencesManager(
            application = application,
            bookPreferencesRepository = bookPreferencesRepository,
            audioNavigatorFactory = audioNavigatorFactory,
            ttsNavigatorFactoryWrapper = ttsNavigatorFactoryWrapper,
            pdfNavigatorFactoryWrapper = pdfNavigatorFactoryWrapper,
            preferencesSerializerFactory = preferencesSerializerFactory,
        )
    }

    @After
    fun tearDown() {
        unmockkObject(ReflowableWebRenditionFactory)
        unmockkObject(FixedWebRenditionFactory)
    }

    /**
     * A simple fake implementation of [Preference] to avoid mocking.
     */
    class FakePreference<T>(
        private var _value: T?,
        private val _effectiveValue: T,
    ) : Preference<T> {
        override val value: T? get() = _value
        override val effectiveValue: T get() = _effectiveValue
        override val isEffective: Boolean = true
        override fun set(value: T?) {
            _value = value
        }
    }

    interface TestReflowableController : NavigationController<ExportableLocation, GoLocation>,
        SettingsController<ReflowableWebSettings>

    interface TestFixedController : NavigationController<ExportableLocation, GoLocation>,
        SettingsController<FixedWebSettings>

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `commitPreferences saves reflowable preferences`() = runTest {
        val bookId = 1L
        val preferences = ReaderPreferences.ReflowableWeb(ReflowableWebPreferences())
        val publication = mockk<Publication>(relaxed = true)
        val controller = mockk<TestReflowableController>(relaxed = true)
        val navigator = ReaderNavigator.New(controller)
        val json = "{}"

        val mockFactory = mockk<ReflowableWebRenditionFactory>(relaxed = true)
        every { ReflowableWebRenditionFactory.invoke(any(), any(), any()) } returns mockFactory
        val mockEditor = mockk<ReflowableWebPreferencesEditor>(relaxed = true)
        every { mockFactory.createPreferencesEditor(any(), any()) } returns mockEditor

        every { reflowableSerializer.serialize(preferences = preferences.value) } returns json

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            publication = publication,
            navigator = navigator,
            audioNavigator = null,
            ttsManager = ttsManager,
        )

        verify { (controller as SettingsController<ReflowableWebSettings>).settings = any() }
        coVerify {
            bookPreferencesRepository.savePreferences(
                bookId = bookId,
                preferencesJson = json,
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `commitPreferences saves fixed preferences`() = runTest {
        val bookId = 1L
        val preferences = ReaderPreferences.FixedWeb(FixedWebPreferences())
        val publication = mockk<Publication>(relaxed = true)
        val controller = mockk<TestFixedController>(relaxed = true)
        val navigator = ReaderNavigator.New(controller)
        val json = "{}"

        val mockFactory = mockk<FixedWebRenditionFactory>(relaxed = true)
        every { FixedWebRenditionFactory.invoke(any(), any(), any()) } returns mockFactory

        every { fixedSerializer.serialize(preferences = preferences.value) } returns json

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            publication = publication,
            navigator = navigator,
            audioNavigator = null,
            ttsManager = ttsManager,
        )

        verify { (controller as SettingsController<FixedWebSettings>).settings = any() }
        coVerify {
            bookPreferencesRepository.savePreferences(
                bookId = bookId,
                preferencesJson = json,
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `commitPreferences saves pdf preferences`() = runTest {
        val bookId = 1L
        val preferences = ReaderPreferences.Pdf(PdfiumPreferences())
        val publication = mockk<Publication>(relaxed = true)
        val mockNav = mockk<VisualNavigator>(
            relaxed = true,
            moreInterfaces = arrayOf(Configurable::class),
        )
        val legacyNav = ReaderNavigator.Legacy(mockNav)
        val json = "{}"
        every { pdfSerializer.serialize(preferences = preferences.value) } returns json

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            publication = publication,
            navigator = legacyNav,
            audioNavigator = null,
            ttsManager = ttsManager,
        )

        verify {
            (mockNav as Configurable<PdfiumSettings, PdfiumPreferences>).submitPreferences(
                preferences = preferences.value,
            )
        }
        coVerify {
            bookPreferencesRepository.savePreferences(
                bookId = bookId,
                preferencesJson = json,
            )
        }
    }

    @Test
    fun `commitPreferences saves tts preferences`() = runTest {
        val bookId = 1L
        val preferences = ReaderPreferences.Tts(AndroidTtsPreferences())
        val publication = mockk<Publication>(relaxed = true)
        val json = "{}"
        every { ttsSerializer.serialize(preferences = preferences.value) } returns json

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            publication = publication,
            navigator = null,
            audioNavigator = null,
            ttsManager = ttsManager,
        )

        verify { ttsManager.submitPreferences(preferences = preferences.value) }
        coVerify {
            bookPreferencesRepository.saveTtsPreferences(
                bookId = bookId,
                preferencesJson = json,
            )
        }
    }

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `commitPreferences saves audio preferences`() = runTest {
        val bookId = 1L
        val preferences = ReaderPreferences.Audio(ExoPlayerPreferences())
        val publication = mockk<Publication>(relaxed = true)
        val audioNavigator = mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>(
            relaxed = true,
        )
        val json = "{}"
        every { audioSerializer.serialize(preferences = preferences.value) } returns json

        manager.commitPreferences(
            bookId = bookId,
            preferences = preferences,
            publication = publication,
            navigator = null,
            audioNavigator = audioNavigator,
            ttsManager = ttsManager,
        )

        verify {
            audioNavigator.submitPreferences(
                preferences = preferences.value,
            )
        }
        coVerify {
            bookPreferencesRepository.saveAudiobookPreferences(
                bookId = bookId,
                preferencesJson = json,
            )
        }
    }

    @Test
    fun `createPreferencesEditor for ReflowableWeb`() {
        val publication = mockk<Publication>(relaxed = true)
        val preferences = ReaderPreferences.ReflowableWeb(ReflowableWebPreferences())

        val mockFactory = mockk<ReflowableWebRenditionFactory>(relaxed = true)
        every { ReflowableWebRenditionFactory.invoke(any(), any(), any()) } returns mockFactory

        val result =
            manager.createPreferencesEditor(publication = publication, preferences = preferences)

        assertNotNull(result)
        assert(result is ReaderPreferencesEditor.ReflowableWeb)
    }

    @Test
    fun `createPreferencesEditor for PDF`() {
        val publication = mockk<Publication>(relaxed = true)
        val preferences = ReaderPreferences.Pdf(PdfiumPreferences())
        val editor = mockk<PdfiumPreferencesEditor>()

        every {
            pdfNavigatorFactoryWrapper.createPreferencesEditor(
                publication = publication,
                initialPreferences = preferences.value,
            )
        } returns editor

        val result =
            manager.createPreferencesEditor(publication = publication, preferences = preferences)

        assertNotNull(result)
        assert(result is ReaderPreferencesEditor.Pdf)
        assertEquals(editor, (result as ReaderPreferencesEditor.Pdf).editor)
    }

    @Test
    fun `loadPreferences for EPUB Reflowable`() = runTest {
        val bookId = 1L
        val publication = mockk<Publication>(relaxed = true) {
            every { conformsTo(profile = Publication.Profile.EPUB) } returns true
            every { metadata.layout } returns Layout.REFLOWABLE
        }
        val json = "{}"
        val preferences = ReflowableWebPreferences()

        coEvery { bookPreferencesRepository.getPreferences(bookId = bookId) } returns json
        every { reflowableSerializer.deserialize(preferences = json) } returns preferences

        val result = manager.loadPreferences(bookId = bookId, publication = publication)

        assert(result is ReaderPreferences.ReflowableWeb)
        assertEquals(preferences, (result as ReaderPreferences.ReflowableWeb).value)
    }

    @Test
    fun `createTtsSettingsSession returns session on success`() = runTest {
        val bookId = 1L
        val publication = mockk<Publication>(relaxed = true)
        val factory = mockk<AndroidTtsNavigatorFactory>(relaxed = true)
        val editor = mockk<AndroidTtsPreferencesEditor>(relaxed = true)
        val preferences = AndroidTtsPreferences()

        coEvery {
            ttsNavigatorFactoryWrapper.createFactory(
                application = application,
                publication = publication,
            )
        } returns factory
        coEvery { bookPreferencesRepository.getTtsPreferences(bookId = bookId) } returns "{}"
        every { ttsSerializer.deserialize(preferences = "{}") } returns preferences
        every { factory.createPreferencesEditor(preferences = preferences) } returns editor

        val fakeLanguage = FakePreference<Language?>(_value = null, _effectiveValue = null)
        every { editor.language } returns fakeLanguage

        val fakeVoices = FakePreference<Map<Language, AndroidTtsEngine.Voice.Id>>(
            _value = emptyMap(),
            _effectiveValue = emptyMap(),
        )
        every { editor.voices } returns fakeVoices

        every { ttsManager.voices } returns emptySet()

        val session = manager.createTtsSettingsSession(
            bookId = bookId,
            publication = publication,
            ttsManager = ttsManager,
            application = application,
        )

        assertNotNull(session)
    }

    @Test
    fun `refreshSessionState refreshes Visual state`() {
        val publication = mockk<Publication>(relaxed = true)
        val preferences = ReaderPreferences.ReflowableWeb(ReflowableWebPreferences())

        val mockFactory = mockk<ReflowableWebRenditionFactory>(relaxed = true)
        every { ReflowableWebRenditionFactory.invoke(any(), any(), any()) } returns mockFactory

        val currentState = ReaderUiState.Visual(
            publication = publication,
            book = mockk(),
            initialLocator = null,
            pdfiumDocumentFactory = mockk(),
            capabilities = ReaderCapabilities(
                isSearchable = true,
                canSpeak = true,
                hasPreferences = true,
            ),
            initialPreferences = preferences,
        )

        val newState =
            manager.refreshSessionState(currentState = currentState, newPreferences = preferences)

        assertNotNull(newState)
        assert((newState as ReaderUiState.Visual).preferencesEditor is ReaderPreferencesEditor.ReflowableWeb)
    }
}
