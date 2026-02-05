package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.adapter.exoplayer.audio.ExoPlayerEngineProvider
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class DefaultAudioNavigatorFactoryWrapperTest {

    private val application: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `default constructor initializes components`() {
        val wrapper = DefaultAudioNavigatorFactoryWrapper()
        assertNotNull(wrapper)
    }

    @Test
    fun `default implementation fails cleanly with mock publication`() = runTest {
        val wrapper = DefaultAudioNavigatorFactoryWrapper()

        val publication = mockk<Publication>(relaxed = true)

        val result = wrapper.createNavigator(
            application = application,
            publication = publication,
            initialLocator = null,
            initialPreferences = null,
        )

        assertTrue("Result should be failure for mocked publication", result is Try.Failure)
    }

    @Test
    fun `createNavigator returns success when factory delegate succeeds`() = runTest {
        val publication = mockk<Publication>(relaxed = true)
        val engineProvider = mockk<ExoPlayerEngineProvider>(relaxed = true)
        val navigator =
            mockk<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>>(relaxed = true)

        val wrapper = DefaultAudioNavigatorFactoryWrapper(
            createEngineProvider = { _ -> engineProvider },
            createNavigator = { _, _, _, _ -> Try.success(success = navigator) },
            createPreferencesEditor = { _, _, _ -> null },
        )

        val result = wrapper.createNavigator(
            application = application,
            publication = publication,
            initialLocator = null,
            initialPreferences = null,
        )

        assertTrue("Result should be success", result is Try.Success)

        if (result is Try.Success) {
            assertEquals(
                navigator,
                result.value,
            )
        }
    }

    @Test
    fun `createNavigator returns failure when factory delegate fails`() = runTest {
        val publication = mockk<Publication>(relaxed = true)
        val engineProvider = mockk<ExoPlayerEngineProvider>(relaxed = true)

        val wrapper = DefaultAudioNavigatorFactoryWrapper(
            createEngineProvider = { _ -> engineProvider },
            createNavigator = { _, _, _, _ -> Try.failure(failure = Exception("Delegate failed")) },
            createPreferencesEditor = { _, _, _ -> null },
        )

        val result = wrapper.createNavigator(
            application = application,
            publication = publication,
            initialLocator = null,
            initialPreferences = null,
        )

        assertTrue("Result should be failure", result is Try.Failure)
        if (result is Try.Failure) {
            assertEquals(
                "Delegate failed",
                result.value.message,
            )
        }
    }

    @Test
    fun `createPreferencesEditor returns editor when factory delegate succeeds`() {
        val publication = mockk<Publication>(relaxed = true)
        val engineProvider = mockk<ExoPlayerEngineProvider>(relaxed = true)
        val editor = mockk<PreferencesEditor<ExoPlayerPreferences>>()

        val wrapper = DefaultAudioNavigatorFactoryWrapper(
            createEngineProvider = { _ -> engineProvider },
            createNavigator = { _, _, _, _ -> Try.failure(failure = Exception("Not used")) },
            createPreferencesEditor = { _, _, _ -> editor },
        )

        val result = wrapper.createPreferencesEditor(
            application = application,
            publication = publication,
            initialPreferences = mockk(),
        )

        assertEquals(
            editor,
            result,
        )
    }
}
