package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.Intent
import android.os.Build
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import androidx.media3.session.MediaSession
import androidx.media3.test.utils.TestExoPlayerBuilder
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.common.MediaNavigator
import org.readium.r2.shared.publication.Publication
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowLooper
import kotlin.uuid.Uuid

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class MediaServiceTest {

    @Test
    fun `onBind returns LocalBinder`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()

        val sessionFactory = mockk<MediaSessionFactory>(relaxed = true)
        val mockSession = mockk<MediaLibrarySession>(relaxed = true)
        every { mockSession.id } returns "test_onBind_session_${Uuid.random()}"
        every {
            sessionFactory.createLibrarySession(
                any(),
                any(),
                any(),
                any(),
            )
        } returns mockSession
        service.mediaSessionFactory = sessionFactory
        service.sessionCallback = mockk(relaxed = true)
        controller.create()

        val intent = Intent(ApplicationProvider.getApplicationContext(), MediaService::class.java)
        val binder = service.onBind(intent)

        assertNotNull(binder)
        assertTrue(binder is MediaService.LocalBinder)
    }

    @Test
    fun `openSession creates and registers session`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()

        val sessionFactory = mockk<MediaSessionFactory>(relaxed = true)
        service.mediaSessionFactory = sessionFactory
        service.sessionCallback = mockk(relaxed = true)

        val player = TestExoPlayerBuilder(ApplicationProvider.getApplicationContext()).build()

        val navigator = mockk<AudioNavigator<*, *>>(relaxed = true)
        every { navigator.asMedia3Player() } returns player

        val session = MediaLibrarySession.Builder(service, player, mockk(relaxed = true))
            .setId("test_session_${Uuid.random()}").build()

        every {
            sessionFactory.createLibrarySession(
                service = any(),
                player = any(),
                callback = any(),
                activityIntent = any(),
            )
        } returns session

        controller.create()

        val binder = service.onBind(intent = null) as MediaService.LocalBinder

        binder.openSession(
            navigator = navigator,
            publication = mockk<Publication>(),
            activityIntent = null,
        )

        ShadowLooper.shadowMainLooper().idle()

        val controllerInfo = mockk<MediaSession.ControllerInfo>(relaxed = true)
        assertNotNull(service.onGetSession(controllerInfo))

        binder.closeSession()
        session.release()
        ShadowLooper.shadowMainLooper().idle()
        player.release()
    }

    @Test
    fun `openSession ignores navigator without Media3Player`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()

        val sessionFactory = mockk<MediaSessionFactory>(relaxed = true)
        service.mediaSessionFactory = sessionFactory
        service.sessionCallback = mockk(relaxed = true)

        // Mock a basic session just to avoid null pointer in onCreate
        val player = TestExoPlayerBuilder(ApplicationProvider.getApplicationContext()).build()
        val session = MediaLibrarySession.Builder(service, player, mockk(relaxed = true))
            .setId("test_session_ignore_${Uuid.random()}").build()
        every {
            sessionFactory.createLibrarySession(
                service = any(),
                player = any(),
                callback = any(),
                activityIntent = any(),
            )
        } returns session

        controller.create()

        val navigator = mockk<MediaNavigator<*, *, *>>(relaxed = true)

        val binder = service.onBind(intent = null) as MediaService.LocalBinder
        binder.openSession(navigator = navigator, publication = mockk<Publication>())

        verify(exactly = 0) {
            sessionFactory.createLibrarySession(
                service = any(),
                player = any(),
                callback = any(),
                activityIntent = any(),
            )
        }
    }

    @Test
    fun `closeSession releases and removes session`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()

        val sessionFactory = mockk<MediaSessionFactory>(relaxed = true)
        service.mediaSessionFactory = sessionFactory
        service.sessionCallback = mockk(relaxed = true)

        val player = TestExoPlayerBuilder(ApplicationProvider.getApplicationContext()).build()

        val navigator = mockk<AudioNavigator<*, *>>(relaxed = true)
        every { navigator.asMedia3Player() } returns player

        val session = MediaLibrarySession.Builder(service, player, mockk(relaxed = true))
            .setId("test_session_close_${Uuid.random()}").build()

        every {
            sessionFactory.createLibrarySession(
                service = any(),
                player = any(),
                callback = any(),
                activityIntent = any(),
            )
        } returns session

        controller.create()

        val binder = service.onBind(intent = null) as MediaService.LocalBinder
        binder.openSession(navigator = navigator, publication = mockk<Publication>())

        ShadowLooper.shadowMainLooper().idle()

        binder.closeSession()
        ShadowLooper.shadowMainLooper().idle()

        player.release()
    }

    @Test
    fun `onDestroy releases session`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()

        val sessionFactory = mockk<MediaSessionFactory>(relaxed = true)
        service.mediaSessionFactory = sessionFactory
        service.sessionCallback = mockk(relaxed = true)

        val player = TestExoPlayerBuilder(ApplicationProvider.getApplicationContext()).build()

        val navigator = mockk<AudioNavigator<*, *>>(relaxed = true)
        every { navigator.asMedia3Player() } returns player

        val session = MediaLibrarySession.Builder(service, player, mockk(relaxed = true))
            .setId("test_session_destroy_${Uuid.random()}").build()

        every {
            sessionFactory.createLibrarySession(
                service = any(),
                player = any(),
                callback = any(),
                activityIntent = any(),
            )
        } returns session

        controller.create()

        val binder = service.onBind(intent = null) as MediaService.LocalBinder
        binder.openSession(navigator = navigator, publication = mockk<Publication>())

        ShadowLooper.shadowMainLooper().idle()

        controller.destroy()
        ShadowLooper.shadowMainLooper().idle()
        player.release()
    }
}
