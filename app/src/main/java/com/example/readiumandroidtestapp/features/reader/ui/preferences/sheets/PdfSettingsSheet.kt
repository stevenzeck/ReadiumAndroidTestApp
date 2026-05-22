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
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.PreferencesEditor

@Composable
fun PdfSettingsSheet(
    editor: PreferencesEditor<PdfiumPreferences>,
    onCommit: (Configurable.Preferences<*>) -> Unit,
) {
    val resources = LocalResources.current
    val commit = { onCommit(editor.preferences) }

    val pdfEditor = editor as? PdfiumPreferencesEditor ?: return

    Column {
        if (pdfEditor.fit.isEffective) {
            EnumPreference(
                title = stringResource(id = R.string.fit),
                preference = pdfEditor.fit,
                onValueChange = { newVal ->
                    pdfEditor.fit.set(newVal)
                    commit()
                },
                formatValue = { formatFit(fit = it, resources = resources) },
            )
        }

        if (pdfEditor.scrollAxis.isEffective) {
            EnumPreference(
                title = stringResource(id = R.string.scroll_axis),
                preference = pdfEditor.scrollAxis,
                onValueChange = { newVal ->
                    pdfEditor.scrollAxis.set(newVal)
                    commit()
                },
                formatValue = { formatScrollAxis(axis = it, resources = resources) },
            )
        }

        if (pdfEditor.pageSpacing.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.page_spacing),
                preference = pdfEditor.pageSpacing,
                onCommit = { commit() },
            )
        }
    }
}
