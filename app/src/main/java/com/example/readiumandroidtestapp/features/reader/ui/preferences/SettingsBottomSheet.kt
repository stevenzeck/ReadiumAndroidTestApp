package com.example.readiumandroidtestapp.features.reader.ui.preferences

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets.AudiobookSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets.EpubSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets.PdfSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets.TtsSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.TtsSettingsSession
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.r2.navigator.epub.EpubPreferencesEditor
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.PreferencesEditor

@Composable
fun SettingsBottomSheet(
    settings: Any,
    isFixedLayout: Boolean,
    onCommit: (Configurable.Preferences<*>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .padding(bottom = 32.dp)
                .verticalScroll(state = rememberScrollState()),
        ) {
            Text(
                text = stringResource(id = R.string.reading_preferences),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(all = 16.dp)
                    .align(Alignment.CenterHorizontally),
            )

            when (settings) {
                is EpubPreferencesEditor -> {
                    EpubSettingsSheet(
                        editor = settings,
                        isFixedLayout = isFixedLayout,
                        onCommit = onCommit,
                    )
                }

                is PdfiumPreferencesEditor -> {
                    PdfSettingsSheet(
                        editor = settings,
                        onCommit = onCommit,
                    )
                }

                is TtsSettingsSession -> {
                    TtsSettingsSheet(
                        session = settings,
                        onCommit = onCommit,
                    )
                }

                is PreferencesEditor<*> -> {
                    if (settings.preferences is ExoPlayerPreferences) {
                        AudiobookSettingsSheet(
                            editor = settings,
                            onCommit = onCommit,
                        )
                    } else {
                        Text(
                            text = stringResource(id = R.string.no_settings_available),
                            modifier = Modifier.padding(all = 16.dp),
                        )
                    }
                }

                else -> Text(
                    text = stringResource(id = R.string.no_settings_available),
                    modifier = Modifier.padding(all = 16.dp),
                )
            }

            Spacer(modifier = Modifier.height(height = 16.dp))

            Button(
                onClick = {
                    if (settings is TtsSettingsSession) {
                        settings.editor.clear()
                        onCommit(settings.editor.preferences)
                    } else if (settings is PreferencesEditor<*>) {
                        settings.clear()
                        onCommit(settings.preferences)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Text(text = stringResource(id = R.string.reset_to_defaults))
            }
        }
    }
}
