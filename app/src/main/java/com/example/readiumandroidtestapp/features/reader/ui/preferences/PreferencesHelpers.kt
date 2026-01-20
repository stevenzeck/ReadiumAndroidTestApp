package com.example.readiumandroidtestapp.features.reader.ui.preferences

import android.content.res.Resources
import com.example.readiumandroidtestapp.R
import org.readium.r2.navigator.preferences.Axis
import org.readium.r2.navigator.preferences.ColumnCount
import org.readium.r2.navigator.preferences.Fit
import org.readium.r2.navigator.preferences.FontFamily
import org.readium.r2.navigator.preferences.Spread
import org.readium.r2.navigator.preferences.TextAlign
import org.readium.r2.navigator.preferences.Theme

fun formatFontFamily(fontFamily: FontFamily?, resources: Resources): String {
    return when (fontFamily) {
        FontFamily.SANS_SERIF -> resources.getString(R.string.font_family_sans_serif)
        FontFamily.SERIF -> resources.getString(R.string.font_family_serif)
        FontFamily.OPEN_DYSLEXIC -> resources.getString(R.string.font_family_open_dyslexic)
        FontFamily.ACCESSIBLE_DFA -> resources.getString(R.string.font_family_accessible_dfa)
        FontFamily.IA_WRITER_DUOSPACE -> resources.getString(R.string.font_family_ia_writer_duospace)
        else -> resources.getString(R.string.font_family_sans_serif)
    }
}

fun formatTextAlign(textAlign: TextAlign?, resources: Resources): String {
    return when (textAlign) {
        TextAlign.START -> resources.getString(R.string.text_align_start)
        TextAlign.LEFT -> resources.getString(R.string.text_align_left)
        TextAlign.RIGHT -> resources.getString(R.string.text_align_right)
        TextAlign.JUSTIFY -> resources.getString(R.string.text_align_justify)
        TextAlign.CENTER -> resources.getString(R.string.text_align_center)
        TextAlign.END -> resources.getString(R.string.text_align_end)
        else -> resources.getString(R.string.text_align_start)
    }
}

fun formatTheme(theme: Theme, resources: Resources): String {
    return when (theme) {
        Theme.LIGHT -> resources.getString(R.string.theme_light)
        Theme.DARK -> resources.getString(R.string.theme_dark)
        Theme.SEPIA -> resources.getString(R.string.theme_sepia)
    }
}

fun formatFit(fit: Fit, resources: Resources): String {
    return when (fit) {
        Fit.CONTAIN -> resources.getString(R.string.fit_contain)
        Fit.COVER -> resources.getString(R.string.fit_cover)
        Fit.WIDTH -> resources.getString(R.string.fit_width)
        Fit.HEIGHT -> resources.getString(R.string.fit_height)
    }
}

fun formatScrollAxis(axis: Axis, resources: Resources): String {
    return when (axis) {
        Axis.VERTICAL -> resources.getString(R.string.scroll_axis_vertical)
        Axis.HORIZONTAL -> resources.getString(R.string.scroll_axis_horizontal)
    }
}

fun formatSpread(spread: Spread, resources: Resources): String {
    return when (spread) {
        Spread.AUTO -> resources.getString(R.string.spread_auto)
        Spread.NEVER -> resources.getString(R.string.spread_never)
        Spread.ALWAYS -> resources.getString(R.string.spread_always)
    }
}

fun formatColumnCount(count: ColumnCount?, resources: Resources): String {
    return when (count) {
        ColumnCount.AUTO -> resources.getString(R.string.column_count_auto)
        ColumnCount.ONE -> resources.getString(R.string.column_count_one)
        ColumnCount.TWO -> resources.getString(R.string.column_count_two)
        null -> resources.getString(R.string.column_count_auto)
    }
}
