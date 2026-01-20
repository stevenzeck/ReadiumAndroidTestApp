package com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.preferences.EnumPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.StepperPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.formatFit
import com.example.readiumandroidtestapp.features.reader.ui.preferences.formatScrollAxis
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.r2.navigator.preferences.Configurable

@Composable
fun PdfSettingsSheet(
    editor: PdfiumPreferencesEditor,
    onCommit: (Configurable.Preferences<*>) -> Unit,
) {
    val resources = LocalResources.current
    val commit = { onCommit(editor.preferences) }

    Column {
        if (editor.fit.isEffective) {
            EnumPreference(
                title = stringResource(id = R.string.fit),
                preference = editor.fit,
                onValueChange = { newVal ->
                    editor.fit.set(newVal)
                    commit()
                },
                formatValue = { formatFit(fit = it, resources = resources) },
            )
        }

        if (editor.scrollAxis.isEffective) {
            EnumPreference(
                title = stringResource(id = R.string.scroll_axis),
                preference = editor.scrollAxis,
                onValueChange = { newVal ->
                    editor.scrollAxis.set(newVal)
                    commit()
                },
                formatValue = { formatScrollAxis(axis = it, resources = resources) },
            )
        }

        if (editor.pageSpacing.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.page_spacing),
                preference = editor.pageSpacing,
                onCommit = commit,
            )
        }
    }
}
