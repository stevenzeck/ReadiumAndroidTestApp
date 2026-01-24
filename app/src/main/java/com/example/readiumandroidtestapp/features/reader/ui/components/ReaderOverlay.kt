package com.example.readiumandroidtestapp.features.reader.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderActions
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderCapabilities

@Composable
fun ReaderOverlay(
    visible: Boolean,
    title: String?,
    capabilities: ReaderCapabilities,
    isTtsActive: Boolean,
    isPlaying: Boolean,
    actions: ReaderActions,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { -it } + fadeIn(),
            exit = slideOutVertically { -it } + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = title ?: "",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = actions.onNavigateBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back),
                            contentDescription = stringResource(id = R.string.back),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                ),
            )
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
        ) {
            HorizontalFloatingToolbar(
                expanded = true,
            ) {
                if (!isTtsActive) {
                    if (capabilities.isSearchable) {
                        IconButton(onClick = actions.onSearchClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.search),
                                contentDescription = stringResource(id = R.string.search),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (capabilities.canSpeak) {
                        IconButton(onClick = actions.onTtsClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.text_to_speech),
                                contentDescription = stringResource(id = R.string.tts),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (capabilities.hasPreferences) {
                        IconButton(onClick = actions.onSettingsClick) {
                            Icon(
                                painter = painterResource(id = R.drawable.settings),
                                contentDescription = stringResource(id = R.string.reading_preferences),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    // Bookmarks/TOC always show
                    IconButton(onClick = actions.onTocClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.toc),
                            contentDescription = stringResource(id = R.string.table_of_contents),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    // TTS Media Controls
                    IconButton(onClick = actions.onTtsPrevious) {
                        Icon(
                            painter = painterResource(id = R.drawable.skip_previous_filled),
                            contentDescription = stringResource(id = R.string.previous_sentence),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    val iconRes = remember(isPlaying) {
                        if (isPlaying) R.drawable.pause_filled else R.drawable.play_arrow_filled
                    }
                    val description = if (isPlaying) R.string.pause else R.string.play

                    IconButton(onClick = actions.onTtsPlayPause) {
                        Icon(
                            painter = painterResource(id = iconRes),
                            contentDescription = stringResource(id = description),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(onClick = actions.onTtsNext) {
                        Icon(
                            painter = painterResource(id = R.drawable.skip_next_filled),
                            contentDescription = stringResource(id = R.string.next_sentence),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(onClick = actions.onTtsStop) {
                        Icon(
                            painter = painterResource(id = R.drawable.stop),
                            contentDescription = stringResource(id = R.string.stop),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(onClick = actions.onSettingsClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.settings),
                            contentDescription = stringResource(id = R.string.reading_preferences),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}
