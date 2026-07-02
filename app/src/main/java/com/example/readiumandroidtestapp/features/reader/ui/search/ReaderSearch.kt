package com.example.readiumandroidtestapp.features.reader.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import com.example.readiumandroidtestapp.features.reader.ui.utils.toAnnotatedString
import org.readium.r2.shared.publication.Locator

@Composable
fun SearchBottomSheet(
    query: String?,
    onQueryChange: (String) -> Unit,
    results: LazyPagingItems<SearchItem>,
    onDismissRequest: () -> Unit,
    onLocatorSelected: (Locator) -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        Column(modifier = Modifier.fillMaxSize()) {
            ReaderSearchBar(
                query = query,
                onQueryChange = onQueryChange,
            )

            ReaderSearchResults(
                results = results,
                onLocatorSelected = onLocatorSelected,
            )
        }
    }
}

@Composable
private fun ReaderSearchBar(
    query: String?,
    onQueryChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = query ?: "",
        onValueChange = onQueryChange,
        placeholder = { Text(text = stringResource(id = R.string.search)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 16.dp),
        singleLine = true,
    )
}

@Composable
private fun ReaderSearchResults(
    results: LazyPagingItems<SearchItem>,
    onLocatorSelected: (Locator) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            count = results.itemCount,
        ) { index ->
            when (val item = results[index]) {
                is SearchItem.Header -> {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(color = MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                is SearchItem.Result -> {
                    ListItem(
                        modifier = Modifier.clickable { onLocatorSelected(item.locator) },
                        content = {
                            Text(
                                text = item.locator.text.toAnnotatedString(),
                            )
                        },
                    )
                }

                null -> {
                    // Placeholder
                }
            }
        }
    }
}
