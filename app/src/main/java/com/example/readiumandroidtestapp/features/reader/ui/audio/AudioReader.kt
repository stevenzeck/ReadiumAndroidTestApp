package com.example.readiumandroidtestapp.features.reader.ui.audio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.readiumandroidtestapp.features.reader.ui.preferences.SettingsBottomSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettingsSheet
import org.readium.r2.navigator.preferences.Configurable

@Composable
fun AudioReader(
    book: Book,
    settingsSheetState: ReaderSettingsSheet?,
    onNavigateBack: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsChange: (Configurable.Preferences<*>) -> Unit,
    onSettingsDismiss: () -> Unit,
    onOpenPlayer: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

            Button(
                onClick = onOpenPlayer,
                modifier = Modifier.padding(16.dp),
            ) {
                Text(text = "Play", style = MaterialTheme.typography.labelLarge)
            }

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
