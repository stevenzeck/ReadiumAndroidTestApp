package com.example.readiumandroidtestapp.features.catalogs.ui.publication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.app.AppViewModel
import com.example.readiumandroidtestapp.core.ui.common.BookItem
import com.example.readiumandroidtestapp.core.ui.common.ReadiumScaffold
import org.json.JSONObject
import org.readium.r2.shared.publication.Manifest
import timber.log.Timber

@Composable
fun PublicationDetailScreen(
    manifestJson: String,
    onNavigateBack: () -> Unit,
    appViewModel: AppViewModel = hiltViewModel(),
) {
    val manifestJsonObj = remember(key1 = manifestJson) {
        try {
            JSONObject(manifestJson)
        } catch (e: Exception) {
            Timber.e(e)
            null
        }
    }

    val manifest = remember(key1 = manifestJsonObj) {
        manifestJsonObj?.let { Manifest.fromJSON(json = it) }
    }

    PublicationDetailContent(
        manifest = manifest,
        onNavigateBack = onNavigateBack,
        onImportBook = appViewModel::importBook,
    )
}

@Composable
fun PublicationDetailContent(
    manifest: Manifest?,
    onNavigateBack: () -> Unit,
    onImportBook: (String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    if (manifest == null) {
        Text(text = stringResource(id = R.string.publication_detail_error))
        return
    }

    val coverUrl = remember(key1 = manifest) {
        val imageLinks = manifest.subcollections["images"]?.flatMap { it.links } ?: emptyList()
        imageLinks.maxByOrNull { it.width ?: 0 }?.href?.toString()
            ?: imageLinks.lastOrNull()?.href?.toString()
    }

    ReadiumScaffold(
        title = stringResource(id = R.string.details),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(
                onClick = onNavigateBack,
                shape = RoundedCornerShape(size = 12.dp),
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.arrow_back),
                    contentDescription = stringResource(id = R.string.back),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(state = rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Box(modifier = Modifier.width(width = 240.dp)) {
                    BookItem(
                        title = "",
                        coverModel = coverUrl,
                        onClick = {},
                        modifier = Modifier.padding(bottom = 24.dp),
                    )
                }

                Text(
                    text = manifest.metadata.title ?: stringResource(id = R.string.unknown_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                manifest.metadata.description?.let { description ->
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 16.dp),
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(all = 24.dp),
            ) {
                Button(
                    onClick = {
                        val acquisitionLink = manifest.links.firstOrNull { link ->
                            link.rels.any { it.startsWith(prefix = "http://opds-spec.org/acquisition") } || link.mediaType?.isOpds == true
                        }
                        val acquisitionUrl = acquisitionLink?.href?.toString()

                        if (acquisitionUrl != null) {
                            onImportBook(acquisitionUrl)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(text = stringResource(id = R.string.download))
                }
            }
        }
    }
}
