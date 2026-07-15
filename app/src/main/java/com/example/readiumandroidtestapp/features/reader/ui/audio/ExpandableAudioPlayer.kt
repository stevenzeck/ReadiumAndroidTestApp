package com.example.readiumandroidtestapp.features.reader.ui.audio

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.audio.components.AudioCoverArt
import com.example.readiumandroidtestapp.features.reader.ui.audio.components.AudioMiniPlayer
import com.example.readiumandroidtestapp.features.reader.ui.audio.components.AudioPlayerControls
import com.example.readiumandroidtestapp.features.reader.ui.audio.components.AudioProgressBar
import com.example.readiumandroidtestapp.features.reader.ui.preferences.StepperPreference
import kotlinx.coroutines.launch
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesEditor
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.common.MediaNavigator
import org.readium.r2.navigator.preferences.PreferencesEditor
import org.readium.r2.shared.publication.Publication
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpandableAudioPlayer(
    book: Book,
    publication: Publication?,
    navigator: AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>,
    editor: PreferencesEditor<ExoPlayerPreferences>?,
    sheetState: SheetState,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playback by navigator.playback.collectAsState()
    val scope = rememberCoroutineScope()

    val currentDuration = navigator.readingOrder.items.getOrNull(index = playback.index)?.duration
    val isPlaying =
        playback.playWhenReady && playback.state !is MediaNavigator.State.Failure && playback.state !is MediaNavigator.State.Ended

    val isExpanded by remember(sheetState) {
        derivedStateOf {
            sheetState.targetValue == SheetValue.Expanded || sheetState.currentValue == SheetValue.Expanded
        }
    }

    BackHandler(enabled = isExpanded) {
        onCollapse()
    }

    Crossfade(
        targetState = isExpanded,
        label = "PlayerCrossfade",
        animationSpec = tween(durationMillis = 500),
    ) { expanded ->
        if (!expanded) {
            // Mini Player mode
            AudioMiniPlayer(
                book = book,
                publication = publication,
                navigator = navigator,
                onClick = onExpand,
                modifier = modifier,
            )
        } else {
            // Expanded Player Mode
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    // Use unbounded height so it doesn't squish while dragging up
                    .padding(horizontal = 24.dp)
                    .verticalScroll(state = rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onCollapse) {
                        Icon(
                            painter = painterResource(id = R.drawable.chevron_down),
                            contentDescription = stringResource(id = R.string.minimize),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = book.title ?: stringResource(id = R.string.unknown_title),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 8.dp),
                        textAlign = TextAlign.Center,
                    )
                    // Placeholder for symmetry
                    Spacer(modifier = Modifier.padding(all = 24.dp))
                }

                Spacer(modifier = Modifier.height(32.dp))

                AudioCoverArt(
                    book = book,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(horizontal = 16.dp),
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = book.title ?: stringResource(id = R.string.unknown_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = book.author ?: stringResource(id = R.string.unknown_author),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Spacer(modifier = Modifier.height(32.dp))

                AudioProgressBar(
                    currentOffset = playback.offset,
                    duration = currentDuration,
                    onSeek = { offset ->
                        scope.launch { navigator.skipTo(index = playback.index, offset = offset) }
                    },
                )

                Spacer(modifier = Modifier.height(24.dp))

                AudioPlayerControls(
                    isPlaying = isPlaying,
                    onSkipPrevious = {
                        scope.launch {
                            val newIndex = (playback.index - 1).coerceAtLeast(minimumValue = 0)
                            if (newIndex != playback.index) {
                                navigator.skipTo(index = newIndex, offset = Duration.ZERO)
                            }
                        }
                    },
                    onRewind = {
                        scope.launch { navigator.skip(duration = (-30).seconds) }
                    },
                    onPlayPause = {
                        if (playback.playWhenReady) navigator.pause() else navigator.play()
                    },
                    onForward = {
                        scope.launch { navigator.skip(duration = 30.seconds) }
                    },
                    onSkipNext = {
                        scope.launch {
                            val newIndex =
                                (playback.index + 1).coerceAtMost(maximumValue = navigator.readingOrder.items.lastIndex)
                            if (newIndex != playback.index) {
                                navigator.skipTo(index = newIndex, offset = Duration.ZERO)
                            }
                        }
                    },
                )

                if (editor is ExoPlayerPreferencesEditor) {
                    Spacer(modifier = Modifier.height(24.dp))

                    if (editor.speed.isEffective) {
                        StepperPreference(
                            title = stringResource(id = R.string.speed),
                            preference = editor.speed,
                            onCommit = { navigator.submitPreferences(editor.preferences) },
                            formatValue = { "%.1fx".format(it) },
                        )
                    }

                    if (editor.pitch.isEffective) {
                        StepperPreference(
                            title = stringResource(id = R.string.pitch),
                            preference = editor.pitch,
                            onCommit = { navigator.submitPreferences(editor.preferences) },
                            formatValue = { "%.1fx".format(it) },
                        )
                    }
                }

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }
}
