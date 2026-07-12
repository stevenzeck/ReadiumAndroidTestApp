package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.audio.ReaderMediaBinder
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.Asset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioPlaybackManager @Inject constructor(
    private val mediaBinder: ReaderMediaBinder,
) {
    val expandPlayerEvent: SharedFlow<Unit> field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val book: StateFlow<Book?> field = MutableStateFlow<Book?>(null)

    val publication: StateFlow<Publication?> field = MutableStateFlow<Publication?>(null)

    val navigator: StateFlow<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>?> field = MutableStateFlow<AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>?>(
        null,
    )

    private var currentAsset: Asset? = null

    fun load(
        book: Book,
        publication: Publication,
        asset: Asset,
        audioNavigator: AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>,
    ) {
        if (this.book.value?.id == book.id) return // Already playing this book

        // Close previous resources
        close()

        this.book.value = book
        this.publication.value = publication
        this.currentAsset = asset
        this.navigator.value = audioNavigator

        mediaBinder.bind(navigator = audioNavigator)
    }

    fun close() {
        if (this.navigator.value == null) return

        mediaBinder.unbind()
        this.navigator.value?.close()
        this.publication.value?.close()
        currentAsset?.close()

        this.book.value = null
        this.publication.value = null
        this.navigator.value = null
        currentAsset = null
    }

    fun expandPlayer() {
        expandPlayerEvent.tryEmit(Unit)
    }
}
