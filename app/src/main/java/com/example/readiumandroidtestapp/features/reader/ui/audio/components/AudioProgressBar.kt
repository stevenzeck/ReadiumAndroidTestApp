package com.example.readiumandroidtestapp.features.reader.ui.audio.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun AudioProgressBar(
    currentOffset: Duration,
    duration: Duration?,
    onSeek: (Duration) -> Unit,
    modifier: Modifier = Modifier,
) {
    var sliderPosition by remember { mutableFloatStateOf(value = 0f) }
    var isDragging by remember { mutableStateOf(value = false) }

    LaunchedEffect(key1 = currentOffset, key2 = isDragging) {
        if (!isDragging) {
            sliderPosition = currentOffset.inWholeSeconds.toFloat()
        }
    }

    val totalSeconds = duration?.inWholeSeconds?.toFloat() ?: 1f
    // Ensure we don't divide by zero or have invalid range
    val safeTotalSeconds = totalSeconds.coerceAtLeast(minimumValue = 1f)

    Column(modifier = modifier.fillMaxWidth()) {
        Slider(
            value = sliderPosition,
            onValueChange = { pos ->
                isDragging = true
                sliderPosition = pos
            },
            onValueChangeFinished = {
                onSeek(sliderPosition.toLong().seconds)
                isDragging = false
            },
            valueRange = 0f..safeTotalSeconds,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = formatTime(seconds = sliderPosition.toLong()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = formatTime(seconds = safeTotalSeconds.toLong()),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun formatTime(seconds: Long): String {
    val duration = seconds.toDuration(unit = DurationUnit.SECONDS)
    val h = duration.inWholeHours
    val m = duration.inWholeMinutes % 60
    val s = duration.inWholeSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
