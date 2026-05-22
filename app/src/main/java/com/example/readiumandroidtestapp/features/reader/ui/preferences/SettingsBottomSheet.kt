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
import com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets.FixedWebSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets.PdfSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets.ReflowableWebSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets.TtsSettingsSheet
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferences
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderPreferencesEditor
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderSettings
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.navigator.media.tts.android.AndroidTtsPreferences

@Composable
fun SettingsBottomSheet(
    settings: ReaderSettings,
    onCommit: (ReaderPreferences) -> Unit,
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
                is ReaderSettings.Configurable -> {
                    when (val editor = settings.editor) {
                        is ReaderPreferencesEditor.ReflowableWeb -> {
                            ReflowableWebSettingsSheet(
                                editor = editor.editor,
                                onCommit = {
                                    onCommit(
                                        ReaderPreferences.ReflowableWeb(value = editor.editor.preferences),
                                    )
                                },
                            )
                        }

                        is ReaderPreferencesEditor.FixedWeb -> {
                            FixedWebSettingsSheet(
                                editor = editor.editor,
                                onCommit = {
                                    onCommit(
                                        ReaderPreferences.FixedWeb(value = editor.editor.preferences),
                                    )
                                },
                            )
                        }

                        is ReaderPreferencesEditor.Pdf -> {
                            PdfSettingsSheet(
                                editor = editor.editor,
                                onCommit = {
                                    onCommit(
                                        ReaderPreferences.Pdf(value = it as PdfiumPreferences),
                                    )
                                },
                            )
                        }

                        is ReaderPreferencesEditor.Audio -> {
                            AudiobookSettingsSheet(
                                editor = editor.editor,
                                onCommit = {
                                    onCommit(
                                        ReaderPreferences.Audio(value = it as ExoPlayerPreferences),
                                    )
                                },
                            )
                        }
                    }
                }

                is ReaderSettings.Tts -> {
                    TtsSettingsSheet(
                        session = settings.session,
                        onCommit = {
                            onCommit(
                                ReaderPreferences.Tts(value = it as AndroidTtsPreferences),
                            )
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(height = 16.dp))

            Button(
                onClick = {
                    when (settings) {
                        is ReaderSettings.Tts -> {
                            settings.session.editor.clear()
                            onCommit(ReaderPreferences.Tts(value = settings.session.editor.preferences))
                        }

                        is ReaderSettings.Configurable -> {
                            when (val editor = settings.editor) {
                                is ReaderPreferencesEditor.ReflowableWeb -> {
                                    editor.editor.clear()
                                    onCommit(
                                        ReaderPreferences.ReflowableWeb(value = editor.editor.preferences),
                                    )
                                }

                                is ReaderPreferencesEditor.FixedWeb -> {
                                    editor.editor.clear()
                                    onCommit(
                                        ReaderPreferences.FixedWeb(value = editor.editor.preferences),
                                    )
                                }

                                is ReaderPreferencesEditor.Pdf -> {
                                    editor.editor.clear()
                                    onCommit(
                                        ReaderPreferences.Pdf(value = editor.editor.preferences),
                                    )
                                }

                                is ReaderPreferencesEditor.Audio -> {
                                    editor.editor.clear()
                                    onCommit(
                                        ReaderPreferences.Audio(value = editor.editor.preferences),
                                    )
                                }
                            }
                        }
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
