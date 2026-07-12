package com.example.readiumandroidtestapp.features.bookshelf.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.designsystem.components.BookItem
import com.example.readiumandroidtestapp.core.domain.model.Book
import java.io.File

/**
 * Renders the list of books in a responsive grid layout.
 *
 * @param books The list of [Book] domain models to display.
 * @param onBookClick Callback triggered when a book cover is tapped.
 * @param onMenuClick Callback triggered when the 'more options' menu on a book is accessed.
 */
@Composable
fun BooksGrid(
    books: List<Book>,
    onBookClick: (Book) -> Unit,
    onMenuClick: (Book) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp),
        contentPadding = PaddingValues(all = 16.dp),
        verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        items(items = books, key = { it.id }) { book ->
            val coverFile = remember(book.cover) { book.cover?.let { File(it) } }
            BookItem(
                title = book.title ?: "",
                coverModel = coverFile,
                onClick = { onBookClick(book) },
                menuContent = { dismiss ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = stringResource(id = R.string.delete),
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            )
                        },
                        onClick = {
                            dismiss()
                            onMenuClick(book)
                        },
                        leadingIcon = {
                            Icon(
                                painter = painterResource(id = R.drawable.close),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        contentPadding = MenuDefaults.DropdownMenuItemContentPadding,
                    )
                },
            )
        }
    }
}
