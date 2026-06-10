package com.example.readiumandroidtestapp.features.reader.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation

@Composable
fun AnnotationDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int, ReaderAnnotation.Style) -> Unit, // Note, Color, Style
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = { Text(text = stringResource(id = R.string.add_annotation)) },
        text = {
            AnnotationDialogContent(
                onDismiss = onDismiss,
                onSave = onSave,
            )
        },
    )
}

@Composable
fun AnnotationDialogContent(
    onDismiss: () -> Unit,
    onSave: (String, Int, ReaderAnnotation.Style) -> Unit,
) {
    var note by remember { mutableStateOf(value = "") }
    val colors = listOf(
        Color(color = 0xFFFFFF00), // Yellow
        Color(color = 0xFF00FF00), // Green
        Color(color = 0xFF00FFFF), // Blue
        Color(color = 0xFFFF0000), // Red
        Color(color = 0xFF800080), // Purple
    )
    var selectedColor by remember { mutableStateOf(value = colors[0]) }
    var selectedStyle by remember { mutableStateOf(value = ReaderAnnotation.Style.HIGHLIGHT) }

    Column {
        // Color Circles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(size = 40.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (selectedColor == color) 3.dp else 1.dp,
                            color = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = CircleShape,
                        )
                        .clickable { selectedColor = color },
                )
            }
        }

        Spacer(modifier = Modifier.height(height = 16.dp))

        // Markup Type selector (Highlight / Underline)
        Text(
            text = stringResource(id = R.string.annotation_style),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ReaderAnnotation.Style.entries.forEach { style ->
                val isSelected = selectedStyle == style
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .background(color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                            shape = RoundedCornerShape(size = 8.dp)
                        )
                        .clickable { selectedStyle = style }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = style.name.lowercase().replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(height = 16.dp))

        // Note Input
        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text(text = stringResource(id = R.string.add_note)) },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5,
        )

        Spacer(modifier = Modifier.height(height = 24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(id = R.string.cancel))
            }
            Button(onClick = { onSave(note, selectedColor.toArgb(), selectedStyle) }) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}
