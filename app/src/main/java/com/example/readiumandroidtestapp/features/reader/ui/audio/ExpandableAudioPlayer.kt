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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.media3.cast.MediaRouteButton
import androidx.media3.cast.rememberMediaRouteButtonState
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.ui.compose.material3.MiniController
import androidx.media3.ui.compose.material3.buttons.NextButton
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.buttons.PreviousButton
import androidx.media3.ui.compose.material3.buttons.SeekBackButton
import androidx.media3.ui.compose.material3.buttons.SeekForwardButton
import androidx.media3.ui.compose.material3.indicator.DurationText
import androidx.media3.ui.compose.material3.indicator.PositionText
import androidx.media3.ui.compose.material3.indicator.ProgressSlider
import coil3.compose.rememberAsyncImagePainter
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.audio.components.AudioCoverArt
import com.example.readiumandroidtestapp.features.reader.ui.preferences.StepperPreference
import com.google.android.gms.cast.framework.CastContext
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesEditor
import org.readium.adapter.exoplayer.audio.ExoPlayerSettings
import org.readium.navigator.media.audio.AudioNavigator
import org.readium.r2.navigator.preferences.PreferencesEditor
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@UnstableApi
@Composable
fun ExpandableAudioPlayer(
    book: Book,
    navigator: AudioNavigator<ExoPlayerSettings, ExoPlayerPreferences>,
    editor: PreferencesEditor<ExoPlayerPreferences>?,
    mediaController: MediaController?,
    sheetState: SheetState,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    modifier: Modifier = Modifier,
) {

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
            if (mediaController != null) {
                val defaultCover = painterResource(id = R.drawable.book_2)
                val coverArtPainter = rememberAsyncImagePainter(
                    model = book.cover?.let { java.io.File(it) },
                    error = defaultCover,
                    fallback = defaultCover,
                )

                MiniController(
                    player = mediaController,
                    modifier = modifier,
                    defaultArtwork = coverArtPainter,
                    onClick = onExpand,
                )
            }
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

                    val context = LocalContext.current

//                    Cannot get cast to work with on-device server (like ktor)
//                    due to router/gateway configurations and policies
                    val isRemoteUrl = remember(book.href) {
                        book.href.startsWith("http://", ignoreCase = true) ||
                                book.href.startsWith("https://", ignoreCase = true)
                    }

                    val isCastAvailable = remember(isRemoteUrl) {
                        if (!isRemoteUrl) return@remember false
                        try {
                            CastContext.getSharedInstance(
                                context,
                            )
                            true
                        } catch (e: Exception) {
                            Timber.e(e)
                            false
                        }
                    }

                    if (isCastAvailable) {
                        MediaRouteButton(state = rememberMediaRouteButtonState())
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                AudioCoverArt(
                    book = book,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(horizontal = 32.dp, vertical = 16.dp),
                )

                if (mediaController != null) {
                    ProgressSlider(
                        player = mediaController,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        PositionText(
                            player = mediaController,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        DurationText(
                            player = mediaController,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        PreviousButton(player = mediaController, iconSize = 48.dp)
                        SeekBackButton(player = mediaController, iconSize = 48.dp)
                        PlayPauseButton(
                            player = mediaController,
                            modifier = Modifier.size(72.dp),
                            iconSize = 42.dp,
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            ),
                        )
                        SeekForwardButton(player = mediaController, iconSize = 48.dp)
                        NextButton(player = mediaController, iconSize = 48.dp)
                    }
                }

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
