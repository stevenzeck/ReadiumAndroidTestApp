package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaLibraryService.MediaLibrarySession
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class MediaSessionFactory @Inject constructor() {
    fun createLibrarySession(
        service: MediaLibraryService,
        player: Player,
        callback: MediaLibrarySession.Callback,
        activityIntent: Intent? = null,
    ): MediaLibrarySession {
        return MediaLibrarySession.Builder(service, player, callback)
            .setId(Uuid.random().toString())
            .apply {
                if (activityIntent != null) {
                    val pendingIntent = PendingIntent.getActivity(
                        service,
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
