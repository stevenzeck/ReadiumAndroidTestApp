package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.navigator.media.audio.AudioNavigator
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class DefaultReaderMediaBinderTest {

    private val application: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `bind binds to MediaService`() {
        val binder = ReaderMediaBinder(context = application)
        val navigator = mockk<AudioNavigator<*, *>>()

        binder.bind(navigator = navigator)

        val shadowApp = Shadows.shadowOf(application)
        val boundServices = shadowApp.boundServiceConnections
        assertNotNull(boundServices)
    }

    @Test
    fun `bind verifies interactions using mock Context`() {
        val mockContext = mockk<Context>(relaxed = true)
        val binder = ReaderMediaBinder(context = mockContext)
        val navigator = mockk<AudioNavigator<*, *>>()

        val intentSlot = slot<Intent>()
        val connectionSlot = slot<ServiceConnection>()

        every {
            mockContext.bindService(
                capture(intentSlot),
                capture(connectionSlot),
                any<Int>(),
            )
        } returns true

        binder.bind(navigator = navigator)

        verify {
            mockContext.bindService(
                any(),
                any(),
                Context.BIND_AUTO_CREATE,
            )
        }
        assertNotNull(intentSlot.captured)
        assertEquals(MediaService::class.java.name, intentSlot.captured.component?.className)
        assertNotNull(connectionSlot.captured)

        val serviceBinder = mockk<MediaService.LocalBinder>(relaxed = true)

        connectionSlot.captured.onServiceConnected(
            ComponentName(mockContext, MediaService::class.java),
            serviceBinder,
        )

        verify {
            serviceBinder.openSession(
                navigator = navigator,
                activityIntent = any(),
            )
        }
    }

    @Test
    fun `unbind unbinds service`() {
        val mockContext = mockk<Context>(relaxed = true)
        val binder = ReaderMediaBinder(context = mockContext)
        val navigator = mockk<AudioNavigator<*, *>>()

        val connectionSlot = slot<ServiceConnection>()
        every {
            mockContext.bindService(
                any(),
                capture(connectionSlot),
                any<Int>(),
            )
        } returns true

        binder.bind(navigator = navigator)
        binder.unbind()

        verify {
            mockContext.unbindService(connectionSlot.captured)
        }
    }
}
