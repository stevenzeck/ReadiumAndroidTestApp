package com.example.readiumandroidtestapp.features.catalogs.ui.feed.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.domain.model.Catalog

@Composable
fun CatalogItem(
    catalog: Catalog,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ListItem(
        headlineContent = {
            Text(
                text = catalog.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
        },
        supportingContent = {
            Text(
                text = catalog.href,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        trailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                FilledTonalIconButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(size = 12.dp),
                    modifier = Modifier.size(size = 40.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.edit),
                        contentDescription = stringResource(id = R.string.edit_feed),
                        modifier = Modifier.size(size = 20.dp),
                    )
                }
                IconButton(
                    onClick = onDelete,
                    shape = RoundedCornerShape(size = 12.dp),
                    modifier = Modifier.size(size = 40.dp),
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.delete),
                        contentDescription = stringResource(id = R.string.delete_feed),
                        modifier = Modifier.size(size = 20.dp),
                    )
                }
            }
        },
    )
}
