package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import io.mockk.mockk
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MediaServiceTest {

    @Test
    fun `onBind returns LocalBinder`() {
        val controller = Robolectric.buildService(MediaService::class.java)
        val service = controller.get()
        service.mediaSessionFactory = mockk(relaxed = true)

        controller.create()

        val intent = Intent(ApplicationProvider.getApplicationContext(), MediaService::class.java)
        val binder = service.onBind(intent)

        assertNotNull(binder)
        assertTrue(binder is MediaService.LocalBinder)
    }
}
