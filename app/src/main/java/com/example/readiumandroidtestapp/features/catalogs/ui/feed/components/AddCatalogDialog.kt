package com.example.readiumandroidtestapp.features.catalogs.ui.feed.components

import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R

@Composable
fun AddCatalogDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (title: String, url: String) -> Unit,
    initialTitle: String = "",
    initialUrl: String = "",
    urlEnabled: Boolean = true,
    dialogTitle: String = stringResource(id = R.string.add_feed),
    confirmButtonText: String = stringResource(id = R.string.add),
) {
    var title by remember { mutableStateOf(value = initialTitle) }
    var url by remember { mutableStateOf(value = initialUrl) }

    val isFormValid by remember {
        derivedStateOf {
            title.isNotBlank() && url.isNotBlank() && Patterns.WEB_URL.matcher(url).matches()
        }
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = dialogTitle) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(space = 16.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(text = stringResource(id = R.string.feed_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text(text = stringResource(id = R.string.feed_url)) },
                    singleLine = true,
                    enabled = urlEnabled,
                    modifier = Modifier.fillMaxWidth(),
                    isError = url.isNotBlank() && !Patterns.WEB_URL.matcher(url).matches(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(title, url) },
                enabled = isFormValid,
            ) {
                Text(text = confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
    )
}
