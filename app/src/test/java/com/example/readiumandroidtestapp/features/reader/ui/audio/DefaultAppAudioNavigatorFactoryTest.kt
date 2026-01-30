package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.Application
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try

class DefaultAppAudioNavigatorFactoryTest {

    private val application: Application = mockk()
    private val wrapper: AudioNavigatorFactoryWrapper = mockk()
    private val factory =
        DefaultAppAudioNavigatorFactory(context = application, navigatorFactoryWrapper = wrapper)

    @Test
    fun `createNavigator returns success when wrapper succeeds`() = runTest {
        val publication = mockk<Publication>()
        val initialLocator = mockk<Locator>()
        val initialPreferences = mockk<ExoPlayerPreferences>()
        val navigator = mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>()

        coEvery {
            wrapper.createNavigator(
                application = application,
                publication = publication,
                initialLocator = initialLocator,
                initialPreferences = initialPreferences,
            )
        } returns Try.success(success = navigator)

        val result = factory.createNavigator(
            publication = publication,
            initialLocator = initialLocator,
            initialPreferences = initialPreferences,
        )

        assertTrue(result is Try.Success)
        assertEquals(navigator, (result as Try.Success).value)
    }

    @Test
    fun `createNavigator returns failure when wrapper fails`() = runTest {
        val publication = mockk<Publication>()
        coEvery {
            wrapper.createNavigator(
                application = application,
                publication = publication,
                initialLocator = any(),
                initialPreferences = any(),
            )
        } returns Try.failure(failure = Exception("Wrapper failed"))

        val result = factory.createNavigator(
            publication = publication,
            initialLocator = null,
            initialPreferences = null,
        )

        assertTrue(result is Try.Failure)
        assertEquals("Wrapper failed", (result as Try.Failure).value.message)
    }

    @Test
    fun `createPreferencesEditor delegates to wrapper`() {
        val publication = mockk<Publication>()
        val preferences = mockk<ExoPlayerPreferences>()
        val editor = mockk<PreferencesEditor<ExoPlayerPreferences>>()

        every {
            wrapper.createPreferencesEditor(
                application = application,
                publication = publication,
                initialPreferences = preferences,
            )
        } returns editor

        val result = factory.createPreferencesEditor(
            publication = publication,
            initialPreferences = preferences,
        )

        assertEquals(editor, result)
    }
}
