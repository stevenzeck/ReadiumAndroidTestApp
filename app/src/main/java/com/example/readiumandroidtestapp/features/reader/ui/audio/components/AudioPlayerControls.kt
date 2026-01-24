package com.example.readiumandroidtestapp.features.reader.ui.audio.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R

@Composable
fun AudioPlayerControls(
    isPlaying: Boolean,
    onSkipPrevious: () -> Unit,
    onRewind: () -> Unit,
    onPlayPause: () -> Unit,
    onForward: () -> Unit,
    onSkipNext: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Previous Chapter
        IconButton(onClick = onSkipPrevious) {
            Icon(
                painter = painterResource(id = R.drawable.skip_previous_filled),
                contentDescription = stringResource(id = R.string.previous_chapter),
                modifier = Modifier.size(size = 32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Rewind
        IconButton(onClick = onRewind) {
            Icon(
                painter = painterResource(id = R.drawable.replay_30),
                contentDescription = stringResource(id = R.string.rewind_30),
                modifier = Modifier.size(size = 32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Play/Pause
        FloatingActionButton(
            onClick = onPlayPause,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier.size(size = 72.dp),
        ) {
            Icon(
                painter = painterResource(
                    id = if (isPlaying) R.drawable.pause_filled else R.drawable.play_arrow_filled,
                ),
                contentDescription = stringResource(
                    id = if (isPlaying) R.string.pause else R.string.play,
                ),
                modifier = Modifier.size(size = 36.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        // Forward
        IconButton(onClick = onForward) {
            Icon(
                painter = painterResource(id = R.drawable.forward_30),
                contentDescription = stringResource(id = R.string.forward_30),
                modifier = Modifier.size(size = 32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Next Chapter
        IconButton(onClick = onSkipNext) {
            Icon(
                painter = painterResource(id = R.drawable.skip_next_filled),
                contentDescription = stringResource(id = R.string.next_chapter),
                modifier = Modifier.size(size = 32.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
