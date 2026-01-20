package com.example.readiumandroidtestapp.features.reader.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.readium.r2.shared.publication.Locator

fun Locator.Text.toAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        before?.let { append(it) }
        highlight?.let {
            withStyle(
                style = SpanStyle(
                    background = Color.Yellow,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                ),
            ) {
                append(it)
            }
        }
        after?.let { append(it) }
    }
}
