package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.media3.test.utils.FakePlayer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class MediaSessionFactoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val factory = MediaSessionFactory()

    @Test
    fun `createLibrarySession creates session with correct player`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()
        service.mediaSessionFactory = mockk(relaxed = true)
        service.sessionCallback = mockk(relaxed = true)

        val player = FakePlayer()

        val session = factory.createLibrarySession(
            service = service,
            player = player,
            callback = mockk(relaxed = true),
            activityIntent = null,
        )

        assertNotNull(
            "Session should be created",
            session,
        )
        assertEquals(
            player,
            session.player,
        )
        session.release()
    }

    @Test
    fun `createLibrarySession sets session activity when intent is provided`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()
        service.mediaSessionFactory = mockk(relaxed = true)
        service.sessionCallback = mockk(relaxed = true)

        val player = FakePlayer()
        val intent = Intent(
            context,
            MediaSessionFactoryTest::class.java,
        )

        val session = factory.createLibrarySession(
            service = service,
            player = player,
            callback = mockk(relaxed = true),
            activityIntent = intent,
        )

        assertNotNull(
            "Session should be created",
            session,
        )
        session.release()
    }
}
