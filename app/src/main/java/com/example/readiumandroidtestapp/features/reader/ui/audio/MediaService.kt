package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.app.PendingIntent
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import org.readium.navigator.media.common.Media3Adapter
import org.readium.navigator.media.common.MediaNavigator
import timber.log.Timber

/**
 * Android Service responsible for managing background media playback.
 *
 * This service implements the Media3 `MediaSessionService` to support:
 * - Audiobook playback via Readium.
 * - Text-to-Speech (TTS) functionality.
 *
 * It exposes a local [LocalBinder] that allows the UI (via ViewModel) to register
 * a [MediaNavigator]. The service then creates and manages a [MediaSession]
 * backed by the navigator's underlying player.
 */
class MediaService : MediaSessionService() {

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this).build())
    }

    /**
     * Local binder to interact with the service from the same process.
     */
    inner class LocalBinder : Binder() {
        /**
         * Opens a new MediaSession for the given [navigator].
         *
         * This method:
         * 1. Extracts the Media3 Player from the navigator.
         * 2. Creates a MediaSession.
         * 3. Registers the session with the service logic.
         */
        fun openSession(
            navigator: MediaNavigator<*, *, *>,
            activityIntent: Intent? = null,
        ) {
            // Close any existing session to ensure we only have one active session for simplicity.
            closeSession()

            val player = (navigator as? Media3Adapter)?.asMedia3Player()
                ?: run {
                    Timber.e(message = "Navigator does not expose a Media3 Player")
                    return
                }

            val session = MediaSession.Builder(this@MediaService, player)
                .apply {
                    if (activityIntent != null) {
                        val pendingIntent = PendingIntent.getActivity(
                            this@MediaService,
                            0,
                            activityIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                        )
                        setSessionActivity(pendingIntent)
                    }
                }
                .build()

            addSession(session)
            mediaSession = session
        }

        fun closeSession() {
            mediaSession?.run {
                release()
                removeSession(this)
                mediaSession = null
            }
        }
    }

    private val binder = LocalBinder()
    private var mediaSession: MediaSession? = null

    override fun onBind(intent: Intent?): IBinder? {
        // If the intent is for the MediaSessionService interface, delegate to super
        if (intent?.action == SERVICE_INTERFACE) {
            return super.onBind(intent)
        }
        // Otherwise, return our local binder for app interaction
        return binder
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
