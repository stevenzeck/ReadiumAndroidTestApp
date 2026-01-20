package com.example.readiumandroidtestapp.features.reader.ui.preferences.sheets

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.preferences.ChoicePreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.EnumPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.StepperPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.SwitchPreference
import com.example.readiumandroidtestapp.features.reader.ui.preferences.formatColumnCount
import com.example.readiumandroidtestapp.features.reader.ui.preferences.formatFontFamily
import com.example.readiumandroidtestapp.features.reader.ui.preferences.formatSpread
import com.example.readiumandroidtestapp.features.reader.ui.preferences.formatTextAlign
import com.example.readiumandroidtestapp.features.reader.ui.preferences.formatTheme
import org.readium.r2.navigator.epub.EpubPreferencesEditor
import org.readium.r2.navigator.preferences.Configurable
import org.readium.r2.navigator.preferences.FontFamily

@Composable
fun EpubSettingsSheet(
    editor: EpubPreferencesEditor,
    isFixedLayout: Boolean,
    onCommit: (Configurable.Preferences<*>) -> Unit,
) {
    val commit = { onCommit(editor.preferences) }

    if (isFixedLayout) {
        FixedLayoutSettings(editor = editor, onCommit = commit)
    } else {
        ReflowableSettings(editor = editor, onCommit = commit)
    }
}

@Composable
private fun ReflowableSettings(
    editor: EpubPreferencesEditor,
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

        if (editor.spread.isEffective) {
            EnumPreference(
                title = stringResource(id = R.string.spread),
                preference = editor.spread,
                onValueChange = { newVal ->
                    editor.spread.set(newVal)
                    onCommit()
                },
                formatValue = { formatSpread(spread = it, resources = resources) },
            )
        }

        if (editor.theme.isEffective) {
            EnumPreference(
                title = stringResource(id = R.string.theme),
                preference = editor.theme,
                onValueChange = { newVal ->
                    editor.theme.set(newVal)
                    onCommit()
                },
                formatValue = { formatTheme(theme = it, resources = resources) },
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
            EnumPreference(
                title = stringResource(id = R.string.column_count),
                preference = editor.columnCount,
                onValueChange = { newVal ->
                    editor.columnCount.set(newVal)
                    onCommit()
                },
                formatValue = { formatColumnCount(count = it, resources = resources) },
            )
        }

        if (editor.pageMargins.isEffective) {
            StepperPreference(
                title = stringResource(id = R.string.page_margins),
                preference = editor.pageMargins,
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

        if (editor.publisherStyles.isEffective) {
            SwitchPreference(
                title = stringResource(id = R.string.publisher_styles),
                preference = editor.publisherStyles,
                onCheckedChange = { newVal ->
                    editor.publisherStyles.set(newVal)
                    onCommit()
                },
            )
        }
    }
}

@Composable
private fun FixedLayoutSettings(
    editor: EpubPreferencesEditor,
    onCommit: () -> Unit,
) {
    val resources = LocalResources.current

    Column {
        if (editor.spread.isEffective) {
            EnumPreference(
                title = stringResource(id = R.string.spread),
                preference = editor.spread,
                onValueChange = { newVal ->
                    editor.spread.set(newVal)
                    onCommit()
                },
                formatValue = { formatSpread(spread = it, resources = resources) },
            )
        }

        if (editor.theme.isEffective) {
            EnumPreference(
                title = stringResource(id = R.string.theme),
                preference = editor.theme,
                onValueChange = { newVal ->
                    editor.theme.set(newVal)
                    onCommit()
                },
                formatValue = { formatTheme(theme = it, resources = resources) },
            )
        }
    }
}
