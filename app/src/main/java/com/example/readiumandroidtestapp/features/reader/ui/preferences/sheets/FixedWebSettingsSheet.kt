package com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.preferences.EnumPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.SwitchPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.formatFit
import org.readium.navigator.web.fixedlayout.preferences.FixedWebPreferencesEditor

@Composable
fun FixedWebSettingsSheet(
    editor: FixedWebPreferencesEditor,
    onCommit: () -> Unit,
) {
    val resources = LocalResources.current

    Column {
        if (editor.fit.isEffective) {
            EnumPreference(
                title = stringResource(id = R.string.fit),
                preference = editor.fit,
                onValueChange = { newVal ->
                    editor.fit.set(newVal)
                    onCommit()
                },
                formatValue = { formatFit(fit = it, resources = resources) },
            )
        }

        if (editor.spreads.isEffective) {
            SwitchPreference(
                title = stringResource(id = R.string.spread),
                preference = editor.spreads,
                onCheckedChange = { newVal ->
                    editor.spreads.set(newVal)
                    onCommit()
                },
            )
        }
    }
}
