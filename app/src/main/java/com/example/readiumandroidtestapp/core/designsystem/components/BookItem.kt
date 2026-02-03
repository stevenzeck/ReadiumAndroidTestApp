package com.example.readiumandroidtestapp.core.designsystem.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.example.readiumandroidtestapp.R

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookItem(
    title: String,
    coverModel: Any?, // Can be File (Bookshelf) or URL String (OPDS)
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    menuContent: (@Composable (dismiss: () -> Unit) -> Unit)? = null,
) {
    var menuExpanded by remember { mutableStateOf(value = false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(ratio = 2f / 3f),
        ) {
            AsyncImage(
                model = coverModel,
                contentDescription = stringResource(id = R.string.cover_description, title),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                fallback = painterResource(id = R.drawable.book_2),
                error = painterResource(id = R.drawable.book_2),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (title != "") {
                Text(
                    text = title.ifBlank { stringResource(id = R.string.unknown_title) },
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(weight = 1f),
                )
            }

            // Only render the menu button if content is provided
            if (menuContent != null) {
                IconButton(
                    onClick = { menuExpanded = true },
                    shape = RoundedCornerShape(size = 12.dp),
                    modifier = Modifier
                        .offset(x = 12.dp)
                        .size(size = 40.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.more_vert),
                        contentDescription = stringResource(id = R.string.more_options),
                        modifier = Modifier.size(size = 24.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large,
                    ) {
                        menuContent { menuExpanded = false }
                    }
                }
            }
        }
    }
}
