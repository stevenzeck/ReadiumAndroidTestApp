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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R

@Composable
fun HighlightDialog(
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit, // Note, Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = {},
        title = { Text(text = stringResource(id = R.string.add_highlight)) },
        text = {
            HighlightDialogContent(
                onDismiss = onDismiss,
                onSave = onSave,
            )
        },
    )
}

@Composable
fun HighlightDialogContent(
    onDismiss: () -> Unit,
    onSave: (String, Int) -> Unit,
) {
    var note by remember { mutableStateOf(value = "") }
    val colors = listOf(
        Color(color = 0xFFFFFF00), // Yellow
        Color(color = 0xFF00FF00), // Green
        Color(color = 0xFF00FFFF), // Blue
        Color(color = 0xFFFF0000), // Red
        Color(color = 0xFF800080),  // Purple
    )
    var selectedColor by remember { mutableStateOf(value = colors[0]) }

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
            Button(onClick = { onSave(note, selectedColor.toArgb()) }) {
                Text(text = stringResource(id = R.string.save))
            }
        }
    }
}
