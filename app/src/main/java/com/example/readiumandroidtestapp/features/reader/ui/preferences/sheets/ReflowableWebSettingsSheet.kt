package com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.preferences.ChoicePreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.EnumPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.OptionalStepperPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.StepperPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.SwitchPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.formatFontFamily
import com.example.readiumandroidtestapp.features.reader.ui.preferences.formatTextAlign
import org.readium.navigator.web.reflowable.preferences.ReflowableWebPreferencesEditor
import org.readium.r2.navigator.preferences.FontFamily

@Composable
fun ReflowableWebSettingsSheet(
    editor: ReflowableWebPreferencesEditor,
    onCommit: () -> Unit,
) {
    val resources = LocalResources.current

    val fontFamilies = listOf(
        null,
        FontFamily.SANS_SERIF,
        FontFamily.SERIF,
        FontFamily.OPEN_DYSLEXIC,
        FontFamily.IA_WRITER_DUOSPACE,
        FontFamily.ACCESSIBLE_DFA,
    )

    Column {
        if (editor.scroll.isEffective) {
            SwitchPreference(
                title = stringResource(id = R.string.scroll_mode),
                preference = editor.scroll,
                onCheckedChange = { newVal ->
                    editor.scroll.set(newVal)
                    onCommit()
                },
            )
        }

        if (editor.fontSize.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.font_size),
                preference = editor.fontSize,
                onCommit = onCommit,
                formatValue = { "${(it * 100).toInt()}%" },
            )
        }

        if (editor.fontFamily.isEffective) {
            ChoicePreference(
                title = stringResource(id = R.string.font_family),
                preference = editor.fontFamily,
                choices = fontFamilies,
                onValueChange = { newVal ->
                    editor.fontFamily.set(newVal)
                    onCommit()
                },
                formatValue = { formatFontFamily(fontFamily = it, resources) },
            )
        }

        if (editor.fontWeight.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.font_weight),
                preference = editor.fontWeight,
                onCommit = onCommit,
                formatValue = { it.toInt().toString() },
            )
        }

        if (editor.lineHeight.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.line_height),
                preference = editor.lineHeight,
                onCommit = onCommit,
            )
        }

        if (editor.textAlign.isEffective) {
            EnumPreference(
                title = stringResource(id = R.string.text_align),
                preference = editor.textAlign,
                onValueChange = { newVal ->
                    editor.textAlign.set(newVal)
                    onCommit()
                },
                formatValue = { formatTextAlign(textAlign = it, resources = resources) },
            )
        }

        if (editor.hyphens.isEffective) {
            SwitchPreference(
                title = stringResource(id = R.string.hyphens),
                preference = editor.hyphens,
                onCheckedChange = { newVal ->
                    editor.hyphens.set(newVal)
                    onCommit()
                },
            )
        }

        if (editor.ligatures.isEffective) {
            SwitchPreference(
                title = stringResource(id = R.string.ligatures),
                preference = editor.ligatures,
                onCheckedChange = { newVal ->
                    editor.ligatures.set(newVal)
                    onCommit()
                },
            )
        }

        if (editor.columnCount.isEffective) {
            OptionalStepperPreference(
                title = stringResource(id = R.string.column_count),
                preference = editor.columnCount,
                onCommit = onCommit,
            )
        }

        if (editor.minMargins.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.page_margins),
                preference = editor.minMargins,
                onCommit = onCommit,
                formatValue = { it.toString() },
            )
        }

        if (editor.paragraphIndent.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.paragraph_indent),
                preference = editor.paragraphIndent,
                onCommit = onCommit,
            )
        }

        if (editor.paragraphSpacing.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.paragraph_spacing),
                preference = editor.paragraphSpacing,
                onCommit = onCommit,
            )
        }

        if (editor.letterSpacing.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.letter_spacing),
                preference = editor.letterSpacing,
                onCommit = onCommit,
            )
        }

        if (editor.wordSpacing.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.word_spacing),
                preference = editor.wordSpacing,
                onCommit = onCommit,
            )
        }

        if (editor.overridePublisherColors.isEffective) {
            SwitchPreference(
                title = stringResource(id = R.string.publisher_styles),
                preference = editor.overridePublisherColors,
                onCheckedChange = { newVal ->
                    editor.overridePublisherColors.set(newVal)
                    onCommit()
                },
            )
        }
    }
}
