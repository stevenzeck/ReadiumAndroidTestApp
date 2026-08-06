package com.example.readiumandroidtestapp.features.reader.ui.tts

import android.app.Application
import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.navigator.media.tts.AndroidTtsNavigator
import org.readium.navigator.media.tts.AndroidTtsNavigatorFactory
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.navigator.media.tts.android.AndroidTtsEngine.Voice
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.shared.publication.Link
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Language
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class ReaderTtsManagerTest {

    private val applicationContext: Context = mockk<Application>(relaxed = true)
    private val ttsNavigator: AndroidTtsNavigator = mockk(relaxed = true)
    private val manager = ReaderTtsManager(applicationContext = applicationContext)
    private val publication: Publication = mockk(relaxed = true)
    private val visualNavigator: VisualNavigator = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        val textLink = Link(href = Url("test.html")!!, mediaType = MediaType(string = "text/html"))
        every { publication.readingOrder } returns listOf(textLink)
        every { publication.links } returns listOf(textLink)
        every { publication.conformsTo(profile = any()) } returns true
        manager.initFactory(publication = publication)
    }

    @After
    fun teardown() {
    }

    @Test
    fun `start initializes navigator and starts playback`() = runTest(context = testDispatcher) {
        val locator = Locator(
            href = Url(url = "href")!!,
            mediaType = MediaType(string = "text/html")!!,
        )
        coEvery { visualNavigator.firstVisibleElementLocator() } returns locator
        val mockFactory = mockk<AndroidTtsNavigatorFactory>(relaxed = true)
        manager.ttsNavigatorFactory = mockFactory
        coEvery {
            mockFactory.createNavigator(
                listener = any(),
                initialLocator = any<Locator>(),
            )
        } returns Try.success(success = ttsNavigator)

        every { ttsNavigator.currentLocator } returns MutableStateFlow(value = locator)
        val mockPlayback = mockk<TtsNavigator.Playback>(relaxed = true)
        every { ttsNavigator.playback } returns MutableStateFlow(value = mockPlayback)

        manager.start(visualNavigator = visualNavigator, scope = backgroundScope, onStop = {})
        testDispatcher.scheduler.runCurrent()

        verify { ttsNavigator.play() }
        assertTrue(manager.isTtsActive.value)
    }

    @Test
    fun `start does nothing if publication is null`() = runTest(context = testDispatcher) {
        val emptyManager = ReaderTtsManager(applicationContext = applicationContext)
        emptyManager.start(visualNavigator = visualNavigator, scope = backgroundScope, onStop = {})

        val mockFactory = mockk<AndroidTtsNavigatorFactory>(relaxed = true)
        manager.ttsNavigatorFactory = mockFactory
        coVerify(exactly = 0) {
            mockFactory.createNavigator(
                listener = any(),
                initialLocator = any<Locator>(),
            )
        }
    }

    @Test
    fun `play delegates to navigator`() = runTest(context = testDispatcher) {
        startTts()
        manager.play()
        verify { ttsNavigator.play() }
    }

    @Test
    fun `pause delegates to navigator`() = runTest(context = testDispatcher) {
        startTts()
        manager.pause()
        verify { ttsNavigator.pause() }
    }

    @Test
    fun `close closes navigator`() = runTest(context = testDispatcher) {
        startTts()
        manager.close()
        verify { ttsNavigator.close() }
        assertFalse(manager.isTtsActive.value)
    }

    @Test
    fun `submitPreferences delegates to navigator`() = runTest(context = testDispatcher) {
        startTts()
        val prefs = AndroidTtsPreferences()
        manager.submitPreferences(preferences = prefs)
        verify { ttsNavigator.submitPreferences(preferences = prefs) }
    }

    @Test
    fun `getVoices returns voices from navigator`() = runTest(context = testDispatcher) {
        startTts()
        val voice = Voice(
            id = Voice.Id(value = "en-us-1"),
            language = Language(code = "en"),
        )
        every { ttsNavigator.voices } returns setOf(voice)

        assertEquals(setOf(voice), manager.voices)
    }

    private fun startTts() {
        val locator = Locator(
            href = Url(url = "href")!!,
            mediaType = MediaType(string = "text/html")!!,
        )
        coEvery { visualNavigator.firstVisibleElementLocator() } returns locator
        val mockFactory = mockk<AndroidTtsNavigatorFactory>(relaxed = true)
        manager.ttsNavigatorFactory = mockFactory
        coEvery {
            mockFactory.createNavigator(
                listener = any(),
                initialLocator = any<Locator>(),
            )
        } returns Try.success(success = ttsNavigator)
        every { ttsNavigator.currentLocator } returns MutableStateFlow(value = locator)
        val mockPlayback = mockk<TtsNavigator.Playback>(relaxed = true)
        every { ttsNavigator.playback } returns MutableStateFlow(value = mockPlayback)

        manager.start(
            visualNavigator = visualNavigator,
            scope = kotlinx.coroutines.CoroutineScope(context = testDispatcher),
            onStop = {},
        )
        testDispatcher.scheduler.runCurrent()
    }
}
