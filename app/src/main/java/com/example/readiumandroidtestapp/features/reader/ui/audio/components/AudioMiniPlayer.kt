package com.example.readiumandroidtestapp.features.reader.ui.audio.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import kotlinx.coroutines.launch
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.navigator.media.common.MediaNavigator
import kotlin.time.DurationUnit

@Composable
fun AudioMiniPlayer(
    book: Book,
    navigator: AudioNavigator<*, *>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playback by navigator.playback.collectAsState()
    val isPlaying =
        playback.playWhenReady && playback.state !is MediaNavigator.State.Failure && playback.state !is MediaNavigator.State.Ended

    val duration = navigator.readingOrder.items.getOrNull(playback.index)?.duration
    val position = playback.offset

    val progress = if (duration != null && duration.toDouble(DurationUnit.SECONDS) > 0) {
        (position.toDouble(DurationUnit.SECONDS) / duration.toDouble(DurationUnit.SECONDS)).toFloat()
    } else {
        0f
    }

    val scope = androidx.compose.runtime.rememberCoroutineScope()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Cover Art Thumbnail
                AudioCoverArt(
                    book = book,
                    modifier = Modifier.size(48.dp),
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Title and Author
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = book.title ?: stringResource(id = R.string.unknown_title),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = book.author ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                val currentIndex = playback.index
                val itemsCount = navigator.readingOrder.items.size
                val canGoBackward = currentIndex > 0
                val canGoForward = currentIndex < itemsCount - 1

                // Previous
                IconButton(
                    onClick = {
                        scope.launch {
                            val newIndex = (playback.index - 1).coerceAtLeast(minimumValue = 0)
                            if (newIndex != playback.index) {
                                navigator.skipTo(
                                    index = newIndex,
                                    offset = kotlin.time.Duration.ZERO,
                                )
                            }
                        }
                    },
                    enabled = canGoBackward,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.skip_previous_filled),
                        contentDescription = stringResource(id = R.string.previous_chapter),
                        tint = if (canGoBackward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.38f,
                        ),
                    )
                }

                // Play / Pause
                IconButton(
                    onClick = {
                        if (isPlaying) navigator.pause() else navigator.play()
                    },
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isPlaying) R.drawable.pause_filled else R.drawable.play_arrow_filled,
                        ),
                        contentDescription = stringResource(
                            id = if (isPlaying) R.string.pause else R.string.play,
                        ),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                // Next
                IconButton(
                    onClick = {
                        scope.launch {
                            val newIndex =
                                (playback.index + 1).coerceAtMost(maximumValue = itemsCount - 1)
                            if (newIndex != playback.index) {
                                navigator.skipTo(
                                    index = newIndex,
                                    offset = kotlin.time.Duration.ZERO,
                                )
                            }
                        }
                    },
                    enabled = canGoForward,
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.skip_next_filled),
                        contentDescription = stringResource(id = R.string.next_chapter),
                        tint = if (canGoForward) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.38f,
                        ),
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )
        }
    }
}
