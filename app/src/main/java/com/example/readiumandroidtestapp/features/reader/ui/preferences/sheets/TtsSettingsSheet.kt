package com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.preferences.ChoicePreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.StepperPreference
import com.example.readiumandroidtestapp.features.reader.ui.state.TtsSettingsSession
import org.readium.r2.navigator.preferences.Configurable

@Composable
fun TtsSettingsSheet(
    session: TtsSettingsSession,
    onCommit: (Configurable.Preferences<*>) -> Unit,
) {
    val editor = session.editor
    val systemDefault = stringResource(id = R.string.system_default)
    val commit = { onCommit(editor.preferences) }

    if (editor.language.isEffective) {
        ChoicePreference(
            title = stringResource(id = R.string.language),
            preference = editor.language,
            choices = session.availableLanguages,
            onValueChange = { newVal ->
                editor.language.set(newVal)
                commit()
            },
            formatValue = {
                it?.locale?.displayName ?: systemDefault
            },
        )
    }

    if (session.voice.isEffective) {
        ChoicePreference(
            title = stringResource(id = R.string.voice),
            preference = session.voice,
            choices = session.availableVoices,
            onValueChange = { newVal ->
                session.voice.set(newVal)
                commit()
            },
            formatValue = { voice ->
                voice?.language?.locale?.displayName ?: systemDefault
            },
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

    if (editor.speed.isEffective) {
        StepperPreference(
            title = stringResource(id = R.string.speed),
            preference = editor.speed,
            onCommit = { commit() },
            formatValue = { "%.1fx".format(it) },
        )
    }
}
