package com.example.readiumandroidtestapp.features.reader.ui.tts

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.Fragment
import com.example.readiumandroidtestapp.features.reader.domain.TtsNavigatorGateway
import com.example.readiumandroidtestapp.features.reader.domain.TtsServiceGateway
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.navigator.Decoration
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.shared.publication.Publication
import timber.log.Timber
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultReaderTtsManager @Inject constructor(
    private val ttsServiceGateway: TtsServiceGateway,
) : ReaderTtsManager {
    private var ttsNavigator: TtsNavigatorGateway? = null
    private var ttsJob: Job? = null
    private var publication: Publication? = null

    private val _isTtsActive = MutableStateFlow(value = false)
    override val isTtsActive = _isTtsActive.asStateFlow()

    override val ttsPlayback: Flow<Boolean> = _isTtsActive.flatMapLatest { active ->
        if (active) ttsNavigator?.playback ?: emptyFlow()
        else emptyFlow()
    }

    override val voices: Set<AndroidTtsEngine.Voice>
        get() = ttsNavigator?.voices.orEmpty()

    /**
     * Initializes the factory. Should be called when publication is ready.
     */
    override fun initFactory(publication: Publication) {
        this.publication = publication
    }

    override fun start(
        visualNavigator: VisualNavigator,
        scope: CoroutineScope,
        onStop: () -> Unit,
    ) {
        val pub = publication ?: return

        scope.launch {
            val initialLocator = visualNavigator.firstVisibleElementLocator() ?: return@launch

            ttsServiceGateway.createNavigator(
                publication = pub,
                initialLocator = initialLocator,
                listener = object : TtsNavigatorGateway.Listener {
                    override fun onStopRequested() {
                        onStop()
                    }
                },
            ).onSuccess { navigator ->
                ttsNavigator = navigator
                _isTtsActive.value = true
                navigator.play()

                ttsJob?.cancel()
                ttsJob = navigator.currentLocator.onEach { locator ->
                    val decoration = Decoration(
                        id = "tts",
                        locator = locator,
                        style = Decoration.Style.Highlight(tint = Color.Red.toArgb()),
                    )
                    val isFragment = visualNavigator is Fragment
                    val isViewValid =
                        if (isFragment) (visualNavigator as Fragment).view != null else true

                    if (isViewValid) {
                        try {
                            (visualNavigator as? DecorableNavigator)?.applyDecorations(
                                decorations = listOf(decoration),
                                group = "tts",
                            )
                            visualNavigator.go(locator, animated = false)
                        } catch (e: Exception) {
                            Timber.w(message = "Suppressed TTS navigation error: ${e.message}")
                        }
                    }
                }.launchIn(scope)
            }.onFailure {
                Timber.e(message = "Failed to create TTS navigator: $it")
            }
        }
    }

    override fun stop(visualNavigator: VisualNavigator?, scope: CoroutineScope) {
        scope.launch {
            if (visualNavigator != null) {
                try {
                    val isFragment = visualNavigator is Fragment
                    val isViewValid =
                        if (isFragment) (visualNavigator as Fragment).view != null else true

                    if (isViewValid) {
                        (visualNavigator as? DecorableNavigator)?.applyDecorations(
                            decorations = emptyList(),
                            group = "tts",
                        )
                    }
                } catch (e: Exception) {
                    Timber.w(message = "Suppressed TTS decoration cleanup error: ${e.message}")
                }
            }

            ttsNavigator?.close()
            ttsNavigator = null
            _isTtsActive.value = false
            ttsJob?.cancel()
            ttsJob = null
        }
    }

    override fun play() {
        ttsNavigator?.play()
    }

    override fun pause() {
        ttsNavigator?.pause()
    }

    override fun previous() {
        ttsNavigator?.skipToPreviousUtterance()
    }

    override fun next() {
        ttsNavigator?.skipToNextUtterance()
    }

    override fun close() {
        ttsJob?.cancel()
        ttsJob = null

        ttsNavigator?.close()
        ttsNavigator = null
        _isTtsActive.value = false
    }

    override fun submitPreferences(preferences: AndroidTtsPreferences) {
        ttsNavigator?.submitPreferences(preferences)
    }
}
