package com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.preferences.StepperPreference
import org.readium.adapter.exoplayer.audio.ExoPlayerPreferencesEditor
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.PreferencesEditor

@Composable
fun AudiobookSettingsSheet(
    editor: PreferencesEditor<*>,
    onCommit: (Configurable.Preferences<*>) -> Unit,
) {
    if (editor is ExoPlayerPreferencesEditor) {
        val commit = { onCommit(editor.preferences) }

        if (editor.speed.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.speed),
                preference = editor.speed,
                onCommit = { commit() },
                formatValue = { "%.1fx".format(it) },
            )
        }

        if (editor.pitch.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.pitch),
                preference = editor.pitch,
                onCommit = { commit() },
                formatValue = { "%.1fx".format(it) },
            )
        }
    }
}
