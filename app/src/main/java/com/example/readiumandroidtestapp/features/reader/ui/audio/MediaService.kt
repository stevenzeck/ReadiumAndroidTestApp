package com.example.readiumandroidtestapp.features.reader.ui.audio

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.media3.cast.CastPlayer
import androidx.media3.common.C
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.example.readiumandroidtestapp.features.reader.ui.audio.server.LocalAudioServer
import dagger.hilt.android.AndroidEntryPoint
import org.readium.navigator.media.common.Media3Adapter
import org.readium.navigator.media.common.MediaNavigator
import org.readium.r2.shared.publication.Publication
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

    @Inject
    lateinit var localAudioServer: LocalAudioServer

    private var mediaLibrarySession: MediaLibrarySession? = null
    private lateinit var fallbackPlayer: Player
    private var castPlayer: CastPlayer? = null
    private var localPlayer: Player? = null

    override fun onCreate() {
        super.onCreate()
        localAudioServer.start()

        setMediaNotificationProvider(DefaultMediaNotificationProvider.Builder(this).build())

        val player = ExoPlayer.Builder(this).build()
        fallbackPlayer = player
        localPlayer = player

        val session = mediaSessionFactory.createLibrarySession(
            service = this,
            player = player,
            callback = sessionCallback,
        )
        addSession(session)
        mediaLibrarySession = session

        try {
            castPlayer = CastPlayer.Builder(this).build().apply {
                addListener(
                    object : Player.Listener {
                        override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
                            val isRemote =
                                deviceInfo.playbackType == DeviceInfo.PLAYBACK_TYPE_REMOTE
                            val activeLocalPlayer = localPlayer ?: fallbackPlayer
                            val currentPlayer = mediaLibrarySession?.player ?: activeLocalPlayer

                            val newPlayer = if (isRemote) this@apply else activeLocalPlayer

                            if (currentPlayer !== newPlayer) {
                                transferState(previousPlayer = currentPlayer, newPlayer = newPlayer)
                                mediaLibrarySession?.player = newPlayer
                            }
                        }
                    },
                )
            }
        } catch (e: Exception) {
            Timber.e(e)
        }
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
            publication: Publication,
            activityIntent: Intent? = null,
        ) {
            val player = (navigator as? Media3Adapter)?.asMedia3Player()
                ?: run {
                    Timber.e(message = "Navigator does not expose a Media3 Player")
                    return
                }

            localAudioServer.publication = publication

            localPlayer = player

            val session = mediaLibrarySession
            if (session != null) {
                if (castPlayer?.deviceInfo?.playbackType == DeviceInfo.PLAYBACK_TYPE_REMOTE) {
                    session.player = castPlayer!!
                } else {
                    session.player = player
                }
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
            localPlayer = fallbackPlayer
            if (castPlayer?.deviceInfo?.playbackType == DeviceInfo.PLAYBACK_TYPE_REMOTE) {
                mediaLibrarySession?.player = castPlayer!!
            } else {
                mediaLibrarySession?.player = fallbackPlayer
            }
        }
    }

    private fun transferState(previousPlayer: Player, newPlayer: Player) {
        val playWhenReady = previousPlayer.playWhenReady
        val currentItemIndex = previousPlayer.currentMediaItemIndex
        val currentPosition = previousPlayer.currentPosition

        if (newPlayer === castPlayer) {
            val mediaItems = mutableListOf<MediaItem>()
            for (i in 0 until previousPlayer.mediaItemCount) {
                val originalItem = previousPlayer.getMediaItemAt(i)
                val originalUri = originalItem.localConfiguration?.uri?.toString()

                var finalItem = originalItem
                if (originalUri != null && !originalUri.startsWith(
                        prefix = "http",
                        ignoreCase = true,
                    )
                ) {
                    val path = if (originalUri.startsWith(prefix = "file://")) {
                        originalUri.substring(startIndex = "file://".length)
                    } else {
                        originalUri
                    }
                    val remoteUrl = localAudioServer.getServerUrl(filePath = path)
                    if (remoteUrl != null) {
                        Timber.i("Casting local file via Ktor server: $remoteUrl")
                        finalItem = originalItem.buildUpon().setUri(remoteUrl).build()
                    }
                }

                mediaItems.add(finalItem)
            }
            if (mediaItems.isNotEmpty()) {
                val index =
                    if (currentItemIndex != C.INDEX_UNSET) currentItemIndex else 0
                newPlayer.setMediaItems(mediaItems, index, currentPosition)
            }
        } else {
            if (currentItemIndex != C.INDEX_UNSET) {
                newPlayer.seekTo(currentItemIndex, currentPosition)
            }
        }

        newPlayer.prepare()
        newPlayer.playWhenReady = playWhenReady
        previousPlayer.pause()
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        super.onBind(intent)?.let { return it }
        return binder
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        return mediaLibrarySession
    }

    override fun onDestroy() {
        binder.closeSession()
        if (this::localAudioServer.isInitialized) {
            localAudioServer.stop()
        }
        mediaLibrarySession?.let { session ->
            removeSession(session)
            session.release()
            mediaLibrarySession = null
        }
        castPlayer?.release()
        castPlayer = null
        if (this::fallbackPlayer.isInitialized) {
            fallbackPlayer.release()
        }
        super.onDestroy()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        // Do not call super.onTaskRemoved() to prevent the service from stopping itself
    }
}
