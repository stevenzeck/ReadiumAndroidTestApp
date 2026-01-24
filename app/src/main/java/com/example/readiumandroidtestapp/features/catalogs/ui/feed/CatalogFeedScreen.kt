package com.example.readiumandroidtestapp.features.catalogs.ui.feed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.ui.common.EmptyView
import com.example.readiumandroidtestapp.core.ui.common.ErrorView
import com.example.readiumandroidtestapp.core.ui.common.LoadingView
import com.example.readiumandroidtestapp.core.ui.common.ReadiumScaffold
import com.example.readiumandroidtestapp.features.catalogs.ui.feed.components.AddCatalogDialog
import com.example.readiumandroidtestapp.features.catalogs.ui.feed.components.CatalogItem

@Composable
fun CatalogFeedScreen(
    onCatalogClick: (Catalog) -> Unit,
    viewModel: CatalogFeedViewModel = hiltViewModel(),
) {
    val feedUiState by viewModel.catalogsState.collectAsState()
    var catalogToEdit by remember { mutableStateOf<Catalog?>(value = null) }
    var catalogToDelete by remember { mutableStateOf<Catalog?>(value = null) }
    var showAddCatalogDialog by remember { mutableStateOf(value = false) }

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    ReadiumScaffold(
        title = stringResource(id = R.string.catalogs),
        scrollBehavior = scrollBehavior,
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddCatalogDialog = true },
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.add_link),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                text = { Text(text = stringResource(id = R.string.add_feed)) },
            )
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            when (val state = feedUiState) {
                is CatalogFeedUiState.Loading -> {
                    LoadingView(modifier = Modifier.align(alignment = Alignment.Center))
                }

                is CatalogFeedUiState.Error -> {
                    ErrorView(
                        message = stringResource(id = R.string.error_no_feed_data),
                        modifier = Modifier.align(alignment = Alignment.Center),
                    )
                }

                is CatalogFeedUiState.Success -> {
                    val catalogs = state.catalogs
                    if (catalogs.isEmpty()) {
                        EmptyView(
                            message = stringResource(id = R.string.no_catalogs_found),
                            modifier = Modifier.align(alignment = Alignment.Center),
                            iconId = R.drawable.browse,
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(all = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(space = 12.dp),
                        ) {
                            items(items = catalogs) { catalog ->
                                CatalogItem(
                                    catalog = catalog,
                                    onClick = { onCatalogClick(catalog) },
                                    onEdit = { catalogToEdit = catalog },
                                    onDelete = { catalogToDelete = catalog },
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddCatalogDialog) {
        AddCatalogDialog(
            onDismissRequest = { showAddCatalogDialog = false },
            onConfirm = { title, url ->
                viewModel.addCatalog(title = title, url = url)
                showAddCatalogDialog = false
            },
        )
    }

    // Local variable capture for Edit Dialog
    val editingCatalog = catalogToEdit
    if (editingCatalog != null) {
        AddCatalogDialog(
            onDismissRequest = { catalogToEdit = null },
            onConfirm = { newTitle, _ ->
                viewModel.editCatalog(
                    catalog = editingCatalog,
                    newTitle = newTitle,
                )
                catalogToEdit = null
            },
            initialTitle = editingCatalog.title,
            initialUrl = editingCatalog.href,
            urlEnabled = false,
            dialogTitle = stringResource(id = R.string.edit_feed),
            confirmButtonText = stringResource(id = R.string.save),
        )
    }

    // Local variable capture for Delete Dialog
    val deletingCatalog = catalogToDelete
    if (deletingCatalog != null) {
        AlertDialog(
            onDismissRequest = { catalogToDelete = null },
            title = { Text(text = stringResource(id = R.string.delete_feed)) },
            text = { Text(text = stringResource(id = R.string.delete_feed_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCatalog(catalog = deletingCatalog)
                        catalogToDelete = null
                    },
                ) {
                    Text(text = stringResource(id = R.string.delete_feed))
                }
            },
            dismissButton = {
                TextButton(onClick = { catalogToDelete = null }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        )
    }
}
