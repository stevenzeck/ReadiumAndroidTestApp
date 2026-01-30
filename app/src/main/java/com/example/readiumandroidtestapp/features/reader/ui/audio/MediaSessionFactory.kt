package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaSession
import javax.inject.Inject

interface MediaSessionFactory {
    fun createSession(
        context: Context,
        player: Player,
        activityIntent: Intent?,
    ): MediaSession
}

class DefaultMediaSessionFactory @Inject constructor() : MediaSessionFactory {
    override fun createSession(
        context: Context,
        player: Player,
        activityIntent: Intent?
    ): MediaSession {
        return MediaSession.Builder(context, player)
            .apply {
                if (activityIntent != null) {
                    val pendingIntent = PendingIntent.getActivity(
                        context,
                        0,
                        activityIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                    )
                    setSessionActivity(pendingIntent)
                }
            }
            .build()
    }
}
