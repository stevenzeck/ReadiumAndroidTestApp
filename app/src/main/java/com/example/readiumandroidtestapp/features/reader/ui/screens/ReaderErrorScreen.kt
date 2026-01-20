package com.example.readiumandroidtestapp.features.reader.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderError

@Composable
fun ReaderErrorScreen(
    error: ReaderError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val message = resolveErrorMessage(error = error)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(all = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(height = 16.dp))
        Button(onClick = onRetry) {
            Text(text = stringResource(id = R.string.retry))
        }
    }
}

@Composable
private fun resolveErrorMessage(error: ReaderError): String {

    val unknownError = stringResource(id = R.string.unknown_error)

    return when (error) {
        is ReaderError.InvalidBookLocation -> stringResource(id = R.string.invalid_book_location)

        is ReaderError.PublicationOpenFailed -> stringResource(
            id = R.string.failed_publication_opening,
            error.cause.message ?: unknownError,
        )

        is ReaderError.AssetRetrievalFailed -> stringResource(
            id = R.string.failed_asset_retrieval,
            error.cause.message ?: unknownError,
        )

        is ReaderError.NavigatorCreationFailed -> stringResource(
            id = R.string.failed_create_audio_navigator,
            error.cause.message ?: unknownError,
        )
    }
}
