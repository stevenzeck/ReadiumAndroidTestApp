package com.example.readiumandroidtestapp.features.reader.ui.tts

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.features.reader.domain.TtsNavigatorGateway
import com.example.readiumandroidtestapp.features.reader.domain.TtsServiceGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.navigator.media.tts.android.AndroidTtsEngine.Voice
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Language
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
class DefaultReaderTtsManagerTest {

    private val ttsServiceGateway: TtsServiceGateway = mockk()
    private val ttsNavigator: TtsNavigatorGateway = mockk(relaxed = true)
    private val manager = DefaultReaderTtsManager(ttsServiceGateway = ttsServiceGateway)
    private val publication: Publication = mockk()
    private val visualNavigator: VisualNavigator = mockk(relaxed = true)
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        manager.initFactory(publication = publication)
    }

    @Test
    fun `start initializes navigator and starts playback`() = runTest(context = testDispatcher) {
        val locator = Locator(
            href = Url(url = "href")!!,
            mediaType = MediaType(string = "text/html")!!,
        )
        every { visualNavigator.currentLocator } returns MutableStateFlow(value = locator)
        coEvery {
            ttsServiceGateway.createNavigator(
                publication = publication,
                initialLocator = any(),
                listener = any(),
            )
        } returns Result.success(value = ttsNavigator)

        every { ttsNavigator.currentLocator } returns MutableStateFlow(value = locator)
        every { ttsNavigator.playback } returns flowOf(value = true)

        manager.start(visualNavigator = visualNavigator, scope = backgroundScope, onStop = {})

        coVerify {
            ttsServiceGateway.createNavigator(
                publication = publication,
                initialLocator = any(),
                listener = any(),
            )
        }
        verify { ttsNavigator.play() }
        assertTrue(manager.isTtsActive.value)
    }

    @Test
    fun `start does nothing if publication is null`() = runTest(context = testDispatcher) {
        val emptyManager = DefaultReaderTtsManager(ttsServiceGateway = ttsServiceGateway)
        emptyManager.start(visualNavigator = visualNavigator, scope = backgroundScope, onStop = {})

        coVerify(exactly = 0) {
            ttsServiceGateway.createNavigator(
                publication = any(),
                initialLocator = any(),
                listener = any(),
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
    fun `previous delegates to navigator`() = runTest(context = testDispatcher) {
        startTts()
        manager.previous()
        verify { ttsNavigator.skipToPreviousUtterance() }
    }

    @Test
    fun `next delegates to navigator`() = runTest(context = testDispatcher) {
        startTts()
        manager.next()
        verify { ttsNavigator.skipToNextUtterance() }
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
            id = Voice.Id("en-us-1"),
            language = Language("en"),
        )
        every { ttsNavigator.voices } returns setOf(voice)

        assertEquals(setOf(voice), manager.voices)
    }

    private fun startTts() {
        val locator = Locator(
            href = Url(url = "href")!!,
            mediaType = MediaType(string = "text/html")!!,
        )
        every { visualNavigator.currentLocator } returns MutableStateFlow(value = locator)
        coEvery {
            ttsServiceGateway.createNavigator(
                publication = any(),
                initialLocator = any(),
                listener = any(),
            )
        } returns Result.success(value = ttsNavigator)
        every { ttsNavigator.currentLocator } returns MutableStateFlow(value = locator)
        every { ttsNavigator.playback } returns flowOf(value = true)

        manager.start(
            visualNavigator = visualNavigator,
            scope = kotlinx.coroutines.CoroutineScope(context = testDispatcher),
            onStop = {},
        )
    }
}
