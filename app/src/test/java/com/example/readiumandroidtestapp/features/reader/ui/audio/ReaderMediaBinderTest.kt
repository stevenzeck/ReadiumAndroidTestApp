package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.navigator.media.audio.AudioNavigator
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class ReaderMediaBinderTest {

    private lateinit var application: Application
    private lateinit var binder: ReaderMediaBinder

    @Before
    fun setUp() {
        application = ApplicationProvider.getApplicationContext()
        binder = DefaultReaderMediaBinder(context = application)
    }

    @Test
    fun `bind calls bindService with correct intent`() {
        val navigator = mockk<AudioNavigator<*, *>>()
        binder.bind(navigator = navigator)

        val shadowApp = Shadows.shadowOf(application)
        val boundIntent = shadowApp.nextStartedService

        assertNotNull("Service should be bound", boundIntent)
        assertEquals(MediaService::class.java.name, boundIntent.component?.className)
    }

    @Test
    fun `unbind unbinds service`() {
        val navigator = mockk<AudioNavigator<*, *>>()
        binder.bind(navigator = navigator)

        val shadowApp = Shadows.shadowOf(application)

        val boundConnections = shadowApp.boundServiceConnections
        assertEquals(1, boundConnections.size)

        binder.unbind()

        assertEquals(0, shadowApp.boundServiceConnections.size)
    }
}
