package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import dagger.hilt.android.AndroidEntryPoint
import org.readium.navigator.media.common.Media3Adapter
import org.readium.navigator.media.common.MediaNavigator
import timber.log.Timber
import javax.inject.Inject

/**
 * Android Service responsible for managing background media playback and Android Auto support.
 *
 * This service implements the Media3 [MediaLibraryService] to support:
 * - Audiobook playback via Readium.
 * - Text-to-Speech (TTS) functionality.
 * - Media library browsing & playback for Android Auto.
 */
@AndroidEntryPoint
class MediaService : MediaLibraryService() {

    @Inject
    lateinit var mediaSessionFactory: MediaSessionFactory

    @Inject
    lateinit var sessionCallback: AudiobookLibrarySessionCallback

    private var mediaLibrarySession: MediaLibrarySession? = null
    private var fallbackPlayer: Player? = null

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this).build())

        val player = ExoPlayer.Builder(this).build()
        fallbackPlayer = player

        val session = mediaSessionFactory.createLibrarySession(
            service = this,
            player = player,
            callback = sessionCallback,
        )
        addSession(session)
        mediaLibrarySession = session
    }

    /**
     * Local binder to interact with the service from the same process.
     */
    inner class LocalBinder : Binder() {
        /**
         * Opens or updates the MediaSession for the given [navigator].
         */
        fun openSession(
            navigator: MediaNavigator<*, *, *>,
            activityIntent: Intent? = null,
        ) {
            val player = (navigator as? Media3Adapter)?.asMedia3Player()
                ?: run {
                    Timber.e(message = "Navigator does not expose a Media3 Player")
                    return
                }

            val session = mediaLibrarySession
            if (session != null) {
                session.player = player
            } else {
                val newSession = mediaSessionFactory.createLibrarySession(
                    service = this@MediaService,
                    player = player,
                    callback = sessionCallback,
                    activityIntent = activityIntent,
                )
                addSession(newSession)
                mediaLibrarySession = newSession
            }
        }

        fun closeSession() {
            fallbackPlayer?.let { player ->
                mediaLibrarySession?.player = player
            }
        }
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder? {
        super.onBind(intent)?.let { return it }
        return binder
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        binder.closeSession()
        mediaLibrarySession?.let { session ->
            removeSession(session)
            session.release()
            mediaLibrarySession = null
        }
        fallbackPlayer?.release()
        fallbackPlayer = null
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Do not call super.onTaskRemoved() to prevent the service from stopping itself
    }
}
