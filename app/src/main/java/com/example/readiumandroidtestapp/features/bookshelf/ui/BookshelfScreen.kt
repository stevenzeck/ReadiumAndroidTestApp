package com.example.readiumandroidtestapp.features.bookshelf.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.designsystem.components.EmptyView
import com.example.readiumandroidtestapp.core.designsystem.components.ErrorView
import com.example.readiumandroidtestapp.core.designsystem.components.LoadingView
import com.example.readiumandroidtestapp.core.designsystem.components.ReadiumScaffold
import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.bookshelf.ui.components.BooksGrid
import com.example.readiumandroidtestapp.features.bookshelf.ui.components.BookshelfFab
import com.example.readiumandroidtestapp.main.MainViewModel

/**
 * The main screen of the application, displaying a grid of imported books.
 *
 * This Composable:
 * - Observes [BookshelfUiState] to render Loading, Empty, Error, or Success views.
 * - Manages the confirmation dialog for deleting a book.
 * - Hosts the [BookshelfFab] for importing new content.
 */
@Composable
@UnstableApi
fun BookshelfScreen(
    onOpenBook: (Book) -> Unit,
    viewModel: BookshelfViewModel = hiltViewModel(),
    mainViewModel: MainViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var bookToDelete by remember { mutableStateOf<Book?>(value = null) }
    var fabExpanded by rememberSaveable { mutableStateOf(value = false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val deviceImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri ->
        uri?.let { mainViewModel.importBook(uri = it) }
    }

    val sharedStorageImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let { mainViewModel.importBook(uri = it) }
    }

    ReadiumScaffold(
        title = stringResource(id = R.string.bookshelf),
        scrollBehavior = scrollBehavior,
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        floatingActionButton = {
            BookshelfFab(
                expanded = fabExpanded,
                onExpandedChange = { fabExpanded = it },
                onImportFromDevice = { deviceImportLauncher.launch(input = "*/*") },
                onImportFromStorage = { sharedStorageImportLauncher.launch(input = arrayOf("*/*")) },
                onImportFromUrl = { url -> mainViewModel.importBook(url = url) },
            )
        },
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            if (uiState.isLoading) {
                LoadingView(modifier = Modifier.align(alignment = Alignment.Center))
            } else if (uiState.error != null) {
                ErrorView(
                    message = stringResource(id = R.string.bookshelf_error),
                    modifier = Modifier.align(alignment = Alignment.Center),
                )
            } else if (uiState.books.isEmpty()) {
                EmptyView(
                    message = stringResource(id = R.string.empty_bookshelf),
                    modifier = Modifier.align(alignment = Alignment.Center),
                )
            } else {
                BooksGrid(
                    books = uiState.books,
                    onBookClick = { book -> onOpenBook(book) },
                    onMenuClick = { bookToDelete = it },
                )
            }

            // Close the FAB when clicking anywhere outside of it
            if (fabExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag(tag = "fab_scrim")
                        .background(color = Color.Black.copy(alpha = 0.2f))
                        .pointerInput(key1 = Unit) {
                            detectTapGestures(onTap = { fabExpanded = false })
                        },
                )
            }
        }
    }

    if (bookToDelete != null) {
        AlertDialog(
            onDismissRequest = { bookToDelete = null },
            title = { Text(text = stringResource(id = R.string.delete_book_title)) },
            text = {
                Text(
                    text = stringResource(
                        id = R.string.delete_book_message,
                        bookToDelete?.title ?: "",
                    ),
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        bookToDelete?.id?.let { viewModel.deleteBook(bookId = it) }
                        bookToDelete = null
                    },
                ) {
                    Text(text = stringResource(id = R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { bookToDelete = null }) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        )
    }
}
