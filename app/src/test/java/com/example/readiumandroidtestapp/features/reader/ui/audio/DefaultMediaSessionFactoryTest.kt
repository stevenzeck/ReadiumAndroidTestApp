package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.Context
import android.content.Intent
import androidx.media3.test.utils.FakePlayer
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultMediaSessionFactoryTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val factory = DefaultMediaSessionFactory()

    @Test
    fun `createSession creates session with correct player`() {
        val player = FakePlayer()

        val session = factory.createSession(
            context = context,
            player = player,
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
    fun `createSession sets session activity when intent is provided`() {
        val player = FakePlayer()
        val intent = Intent(
            context,
            DefaultMediaSessionFactoryTest::class.java,
        )

        val session = factory.createSession(
            context = context,
            player = player,
            activityIntent = intent,
        )

        assertNotNull(
            "Session should be created",
            session,
        )
        session.release()
    }
}
