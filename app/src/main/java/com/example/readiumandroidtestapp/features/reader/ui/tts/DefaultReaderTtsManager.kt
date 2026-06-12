package com.example.readiumandroidtestapp.features.reader.ui.tts

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.fragment.app.Fragment
import com.example.readiumandroidtestapp.features.reader.domain.TtsNavigatorGateway
import com.example.readiumandroidtestapp.features.reader.domain.TtsServiceGateway
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderNavigator
import kotlinx.collections.immutable.persistentListOf
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
import org.readium.navigator.common.Decoration
import org.readium.navigator.common.Progression
import org.readium.navigator.media.tts.android.AndroidTtsEngine
import org.readium.navigator.media.tts.android.AndroidTtsPreferences
import org.readium.navigator.web.fixedlayout.FixedWebDecorationLocation
import org.readium.navigator.web.fixedlayout.FixedWebGoLocation
import org.readium.navigator.web.fixedlayout.FixedWebRenditionController
import org.readium.navigator.web.reflowable.ReflowableWebDecorationLocation
import org.readium.navigator.web.reflowable.ReflowableWebGoLocation
import org.readium.navigator.web.reflowable.ReflowableWebRenditionController
import org.readium.r2.navigator.DecorableNavigator
import org.readium.r2.shared.publication.Locator
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
        navigator: ReaderNavigator,
        scope: CoroutineScope,
        onStop: () -> Unit,
    ) {
        val pub = publication ?: return

        scope.launch {
            val initialLocator = when (navigator) {
                is ReaderNavigator.Legacy -> navigator.navigator.firstVisibleElementLocator()
                is ReaderNavigator.New -> navigator.controller.location.toLocator()
            } ?: return@launch

            ttsServiceGateway.createNavigator(
                publication = pub,
                initialLocator = initialLocator,
                listener = object : TtsNavigatorGateway.Listener {
                    override fun onStopRequested() {
                        onStop()
                    }
                },
            ).onSuccess { ttsNav ->
                ttsNavigator = ttsNav
                _isTtsActive.value = true
                ttsNav.play()

                ttsJob?.cancel()
                when (navigator) {
                    is ReaderNavigator.Legacy -> {
                        val visualNavigator = navigator.navigator
                        ttsJob = ttsNav.currentLocator.onEach { locator ->
                            val decoration = org.readium.r2.navigator.Decoration(
                                id = "tts",
                                locator = locator,
                                style = org.readium.r2.navigator.Decoration.Style.Highlight(tint = Color.Red.toArgb()),
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
                    }

                    is ReaderNavigator.New -> {
                        val controller = navigator.controller
                        var lastUtteranceLocator: Locator? = null
                        ttsJob = ttsNav.location.onEach { location ->
                            val tokenLocator = location.tokenLocator ?: location.utteranceLocator
                            val utteranceLocator = location.utteranceLocator

                            if (utteranceLocator != lastUtteranceLocator) {
                                lastUtteranceLocator = utteranceLocator
                                when (controller) {
                                    is ReflowableWebRenditionController -> {
                                        val progressions = controller.viewport.progressions
                                        val range = progressions[utteranceLocator.href]
                                        val progression =
                                            utteranceLocator.locations.progression?.let {
                                                Progression(
                                                    value = it,
                                                )
                                            }
                                        val alreadyVisible =
                                            progression != null && range != null && progression in range
                                        if (!alreadyVisible) {
                                            controller.goTo(
                                                location = ReflowableWebGoLocation(
                                                    locator = utteranceLocator,
                                                ),
                                            )
                                        }
                                    }

                                    is FixedWebRenditionController -> {
                                        if (utteranceLocator.href != controller.location.href) {
                                            controller.goTo(location = FixedWebGoLocation(locator = utteranceLocator))
                                        }
                                    }
                                }
                            }

                            when (controller) {
                                is ReflowableWebRenditionController -> {
                                    val decLoc = ReflowableWebDecorationLocation(tokenLocator)
                                    if (decLoc != null) {
                                        val decoration = Decoration(
                                            id = Decoration.Id(value = "tts"),
                                            location = decLoc,
                                            style = Decoration.Style.Highlight(tint = 0x50FFEB3B),
                                        )
                                        controller.decorations = controller.decorations.put(
                                            "tts",
                                            persistentListOf(
                                                decoration,
                                            ),
                                        )
                                    } else {
                                        controller.decorations =
                                            controller.decorations.remove("tts")
                                    }
                                }

                                is FixedWebRenditionController -> {
                                    val decLoc = FixedWebDecorationLocation(tokenLocator)
                                    if (decLoc != null) {
                                        val decoration = Decoration(
                                            id = Decoration.Id(value = "tts"),
                                            location = decLoc,
                                            style = Decoration.Style.Highlight(tint = 0x50FFEB3B),
                                        )
                                        controller.decorations = controller.decorations.put(
                                            "tts",
                                            persistentListOf(
                                                decoration,
                                            ),
                                        )
                                    } else {
                                        controller.decorations =
                                            controller.decorations.remove("tts")
                                    }
                                }
                            }
                        }.launchIn(scope)
                    }
                }
            }.onFailure {
                Timber.e(message = "Failed to create TTS navigator: $it")
            }
        }
    }

    override fun stop(navigator: ReaderNavigator?, scope: CoroutineScope) {
        scope.launch {
            if (navigator != null) {
                try {
                    when (navigator) {
                        is ReaderNavigator.Legacy -> {
                            val visualNavigator = navigator.navigator
                            val isFragment = visualNavigator is Fragment
                            val isViewValid =
                                if (isFragment) (visualNavigator as Fragment).view != null else true

                            if (isViewValid) {
                                (visualNavigator as? DecorableNavigator)?.applyDecorations(
                                    decorations = emptyList(),
                                    group = "tts",
                                )
                            }
                        }

                        is ReaderNavigator.New -> {
                            when (val controller = navigator.controller) {
                                is ReflowableWebRenditionController -> {
                                    controller.decorations = controller.decorations.remove("tts")
                                }

                                is FixedWebRenditionController -> {
                                    controller.decorations = controller.decorations.remove("tts")
                                }
                            }
                        }
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
