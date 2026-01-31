package com.example.readiumandroidtestapp.features.reader.data

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.readium.navigator.media.tts.AndroidTtsNavigator
import org.readium.navigator.media.tts.TtsNavigator
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.r2.shared.publication.Locator

class DefaultTtsNavigatorGatewayTest {

    private val navigator: AndroidTtsNavigator = mockk(relaxed = true)
    private val gateway = DefaultTtsNavigatorGateway(navigator)

    @Test
    fun `playback flow maps playWhenReady`() = runTest {
        val playbackState = mockk<TtsNavigator.Playback>()
        every { playbackState.playWhenReady } returns true

        val playbackFlow = MutableStateFlow(value = playbackState)
        every { navigator.playback } returns playbackFlow

        val result = gateway.playback.first()

        assertEquals(true, result)

        val playbackStateFalse = mockk<TtsNavigator.Playback>()
        every { playbackStateFalse.playWhenReady } returns false

        playbackFlow.value = playbackStateFalse
        assertEquals(false, gateway.playback.first())
    }

    @Test
    fun `voices delegates to navigator`() {
        val voice = mockk<AndroidTtsEngine.Voice>()
        val voices = setOf(voice)
        every { navigator.voices } returns voices

        assertEquals(voices, gateway.voices)
    }

    @Test
    fun `currentLocator delegates to navigator`() {
        val locator = mockk<Locator>()
        val locatorFlow = MutableStateFlow(value = locator)
        every { navigator.currentLocator } returns locatorFlow

        assertEquals(locatorFlow, gateway.currentLocator)
    }

    @Test
    fun `play delegates to navigator`() {
        every { navigator.play() } just Runs
        gateway.play()
        verify { navigator.play() }
    }

    @Test
    fun `pause delegates to navigator`() {
        every { navigator.pause() } just Runs
        gateway.pause()
        verify { navigator.pause() }
    }

    @Test
    fun `skipToPreviousUtterance delegates to navigator`() {
        every { navigator.skipToPreviousUtterance() } just Runs
        gateway.skipToPreviousUtterance()
        verify { navigator.skipToPreviousUtterance() }
    }

    @Test
    fun `skipToNextUtterance delegates to navigator`() {
        every { navigator.skipToNextUtterance() } just Runs
        gateway.skipToNextUtterance()
        verify { navigator.skipToNextUtterance() }
    }

    @Test
    fun `close delegates to navigator`() {
        every { navigator.close() } just Runs
        gateway.close()
        verify { navigator.close() }
    }

    @Test
    fun `submitPreferences delegates to navigator`() {
        val preferences = mockk<AndroidTtsPreferences>()
        every { navigator.submitPreferences(preferences = preferences) } just Runs
        gateway.submitPreferences(preferences = preferences)
        verify { navigator.submitPreferences(preferences = preferences) }
    }
}
