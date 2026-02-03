package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Publication

@RunWith(AndroidJUnit4::class)
class DefaultAudioNavigatorFactoryWrapperTest {

    private val application = ApplicationProvider.getApplicationContext<Application>()
    private val wrapper = DefaultAudioNavigatorFactoryWrapper()

    @Test
    fun `createNavigator returns failure for unsupported publication`() = runTest {
        val publication = mockk<Publication>(relaxed = true)

        val result = wrapper.createNavigator(
            application = application,
            publication = publication,
            initialLocator = null,
            initialPreferences = null,
        )

        assertTrue(result.isFailure)
    }
}
