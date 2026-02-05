package com.example.readiumandroidtestapp.features.reader.ui.audio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.designsystem.components.ReadiumScaffold
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.audio.components.AudioCoverArt
import com.example.readiumandroidtestapp.features.reader.ui.audio.components.AudioPlayerControls
import com.example.readiumandroidtestapp.features.reader.ui.audio.components.AudioProgressBar
import com.example.readiumandroidtestapp.features.reader.ui.preferences.SettingsBottomSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettingsSheet
import kotlinx.coroutines.launch
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.common.MediaNavigator
import org.readium.r2.navigator.preferences.Configurable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Composable
fun AudioReader(
    book: Book,
    navigator: AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>,
    settingsSheetState: ReaderSettingsSheet?,
    onNavigateBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsChange: (Configurable.Preferences<*>) -> Unit,
    onSettingsDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playback by navigator.playback.collectAsState()
    val scope = rememberCoroutineScope()

    val currentDuration = navigator.readingOrder.items.getOrNull(index = playback.index)?.duration

    val isPlaying =
        playback.playWhenReady && playback.state !is MediaNavigator.State.Failure && playback.state !is MediaNavigator.State.Ended

    ReadiumScaffold(
        modifier = modifier,
        title = book.title ?: stringResource(id = R.string.unknown_title),
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(id = R.string.back),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    painter = painterResource(id = R.drawable.settings),
                    contentDescription = stringResource(id = R.string.reading_preferences),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AudioCoverArt(
                book = book,
                modifier = Modifier.weight(weight = 1f),
            )

            Spacer(modifier = Modifier.height(height = 32.dp))

            Text(
                text = book.title ?: stringResource(id = R.string.unknown_title),
                style = MaterialTheme.typography.headlineMedium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(height = 8.dp))

            Text(
                text = book.author ?: stringResource(id = R.string.unknown_author),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.height(height = 32.dp))

            AudioProgressBar(
                currentOffset = playback.offset,
                duration = currentDuration,
                onSeek = { offset ->
                    scope.launch {
                        navigator.skipTo(index = playback.index, offset = offset)
                    }
                },
            )

            Spacer(modifier = Modifier.height(height = 24.dp))

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

            Spacer(modifier = Modifier.height(height = 48.dp))
        }

        if (settingsSheetState is ReaderSettingsSheet.Configurable) {
            SettingsBottomSheet(
                settings = settingsSheetState.editor,
                isFixedLayout = false,
                onCommit = { preferences -> onSettingsChange(preferences) },
                onDismissRequest = onSettingsDismiss,
            )
        }
    }
}
