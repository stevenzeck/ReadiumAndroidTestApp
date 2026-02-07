package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.Intent
import android.os.Build
import androidx.media3.session.MediaSession
import androidx.media3.test.utils.TestExoPlayerBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.common.MediaNavigator
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class MediaServiceTest {

    @Test
    fun `onBind returns LocalBinder`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()

        controller.create()
        service.mediaSessionFactory = mockk(relaxed = true)

        val intent = Intent(ApplicationProvider.getApplicationContext(), MediaService::class.java)
        val binder = service.onBind(intent)

        assertNotNull(binder)
        assertTrue(binder is MediaService.LocalBinder)
    }

    @Test
    fun `openSession creates and registers session`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()

        controller.create()

        val sessionFactory = mockk<MediaSessionFactory>()
        service.mediaSessionFactory = sessionFactory

        val player = TestExoPlayerBuilder(ApplicationProvider.getApplicationContext()).build()

        val navigator = mockk<AudioNavigator<*, *>>(relaxed = true)
        every { navigator.asMedia3Player() } returns player

        val session = MediaSession.Builder(service, player).setId("test_session").build()

        every {
            sessionFactory.createSession(
                context = any(),
                player = any(),
                activityIntent = any(),
            )
        } returns session

        val binder = service.onBind(intent = null) as MediaService.LocalBinder

        binder.openSession(navigator = navigator, activityIntent = null)

        ShadowLooper.shadowMainLooper().idle()

        verify {
            sessionFactory.createSession(
                context = any(),
                player = player,
                activityIntent = null,
            )
        }

        val controllerInfo = mockk<MediaSession.ControllerInfo>(relaxed = true)
        assertEquals(session, service.onGetSession(controllerInfo))

        binder.closeSession()
        session.release()
        ShadowLooper.shadowMainLooper().idle()
        player.release()
    }

    @Test
    fun `openSession ignores navigator without Media3Player`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()

        controller.create()

        val sessionFactory = mockk<MediaSessionFactory>(relaxed = true)
        service.mediaSessionFactory = sessionFactory

        val navigator = mockk<MediaNavigator<*, *, *>>(relaxed = true)

        val binder = service.onBind(intent = null) as MediaService.LocalBinder
        binder.openSession(navigator = navigator)

        verify(exactly = 0) {
            sessionFactory.createSession(
                context = any(),
                player = any(),
                activityIntent = any(),
            )
        }
        assertNull(service.onGetSession(controllerInfo = mockk(relaxed = true)))
    }

    @Test
    fun `closeSession releases and removes session`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()

        controller.create()

        val sessionFactory = mockk<MediaSessionFactory>()
        service.mediaSessionFactory = sessionFactory

        val player = TestExoPlayerBuilder(ApplicationProvider.getApplicationContext()).build()

        val navigator = mockk<AudioNavigator<*, *>>(relaxed = true)
        every { navigator.asMedia3Player() } returns player

        val session = MediaSession.Builder(service, player).setId("test_session_close").build()

        every {
            sessionFactory.createSession(
                context = any(),
                player = any(),
                activityIntent = any(),
            )
        } returns session

        val binder = service.onBind(intent = null) as MediaService.LocalBinder
        binder.openSession(navigator = navigator)

        ShadowLooper.shadowMainLooper().idle()
        assertNotNull(service.onGetSession(controllerInfo = mockk(relaxed = true)))

        binder.closeSession()
        ShadowLooper.shadowMainLooper().idle()

        assertNull(service.onGetSession(controllerInfo = mockk(relaxed = true)))

        player.release()
    }

    @Test
    fun `onDestroy releases session`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()

        controller.create()

        val sessionFactory = mockk<MediaSessionFactory>()
        service.mediaSessionFactory = sessionFactory

        val player = TestExoPlayerBuilder(ApplicationProvider.getApplicationContext()).build()

        val navigator = mockk<AudioNavigator<*, *>>(relaxed = true)
        every { navigator.asMedia3Player() } returns player

        val session = MediaSession.Builder(service, player).setId("test_session_destroy").build()

        every {
            sessionFactory.createSession(
                context = any(),
                player = any(),
                activityIntent = any(),
            )
        } returns session

        val binder = service.onBind(intent = null) as MediaService.LocalBinder
        binder.openSession(navigator = navigator)

        ShadowLooper.shadowMainLooper().idle()

        controller.destroy()
        ShadowLooper.shadowMainLooper().idle()
        player.release()
    }
}
