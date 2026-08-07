package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import com.example.readiumandroidtestapp.features.reader.ui.audio.ReaderMediaBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.common.MediaNavigator
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.Asset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlaybackManager @Inject constructor(
    private val mediaBinder: ReaderMediaBinder,
    private val userMessageManager: UserMessageManager,
) {
    val expandPlayerEvent: SharedFlow<Unit> field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val book: StateFlow<Book?> field = MutableStateFlow<Book?>(null)

    val isPlaying: StateFlow<Boolean> field = MutableStateFlow<Boolean>(false)

    val publication: StateFlow<Publication?> field = MutableStateFlow<Publication?>(null)

    val navigator: StateFlow<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>?> field = MutableStateFlow<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>?>(
        null,
    )

    val preferencesEditor: StateFlow<PreferencesEditor<ExoPlayerPreferences>?> field = MutableStateFlow<PreferencesEditor<ExoPlayerPreferences>?>(
        null,
    )

    private var currentAsset: Asset? = null

    private var playbackScope: CoroutineScope? = null

    fun load(
        book: Book,
        publication: Publication,
        asset: Asset,
        audioNavigator: AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>,
        editor: PreferencesEditor<ExoPlayerPreferences>?,
    ) {
        if (this.book.value?.id == book.id) return // Already playing this book

        // Close previous resources
        close()

        this.book.value = book
        this.publication.value = publication
        this.currentAsset = asset
        this.navigator.value = audioNavigator
        this.preferencesEditor.value = editor

        playbackScope = MainScope().apply {
            audioNavigator.playback
                .onEach { playback ->
                    this@AudioPlaybackManager.isPlaying.value = playback.playWhenReady
                    if (playback.state is MediaNavigator.State.Failure) {
                        userMessageManager.emitMessage(messageId = R.string.playback_error)
                    }
                }
                .launchIn(this)
        }

        mediaBinder.bind(navigator = audioNavigator, publication = publication)
    }

    fun close() {
        if (this.navigator.value == null) return

        playbackScope?.cancel()
        playbackScope = null

        mediaBinder.unbind()
        this.navigator.value?.close()
        this.publication.value?.close()
        currentAsset?.close()

        this.book.value = null
        this.publication.value = null
        this.navigator.value = null
        this.preferencesEditor.value = null
        currentAsset = null
    }

    fun expandPlayer() {
        expandPlayerEvent.tryEmit(Unit)
    }
}
