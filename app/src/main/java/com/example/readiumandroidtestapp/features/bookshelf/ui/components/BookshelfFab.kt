package com.example.readiumandroidtestapp.features.bookshelf.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.designsystem.components.SingleTextInputDialog

/**
 * A specialized Floating Action Button menu for importing books.
 *
 * It expands to reveal three import options:
 * 1. Import from URL.
 * 2. Import from Shared Storage (System Picker).
 * 3. Import from Device (File Picker).
 */
@Composable
fun BookshelfFab(
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onImportFromDevice: () -> Unit,
    onImportFromStorage: () -> Unit,
    onImportFromUrl: (String) -> Unit,
) {
    var showUrlDialog by remember { mutableStateOf(value = false) }

    // Closes the menu if the hardware back button is pressed
    BackHandler(enabled = expanded) { onExpandedChange(false) }

    val colors = MaterialTheme.colorScheme
    val menuItems = remember {
        listOf(
            Triple(R.drawable.add_link, R.string.import_from_url) { showUrlDialog = true },
            Triple(R.drawable.storage, R.string.import_from_shared_storage, onImportFromStorage),
            Triple(R.drawable.storage, R.string.import_from_device, onImportFromDevice),
        )
    }

    val expandedState = stringResource(id = R.string.expanded)
    val collapsedState = stringResource(id = R.string.collapsed)
    val toggleDescription = stringResource(id = R.string.toggle_import_menu)

    FloatingActionButtonMenu(
        modifier = Modifier,
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                checked = expanded,
                onCheckedChange = { onExpandedChange(it) },
                containerColor = {
                    lerp(
                        start = colors.primaryContainer,
                        stop = colors.tertiaryContainer,
                        fraction = it,
                    )
                },
                containerCornerRadius = { lerp(start = 28.dp, stop = 12.dp, fraction = it) },
                containerSize = { lerp(start = 56.dp, stop = 64.dp, fraction = it) },
                modifier = Modifier.semantics {
                    stateDescription = if (expanded) expandedState else collapsedState
                    contentDescription = toggleDescription
                },
            ) {
                Icon(
                    painter = painterResource(id = if (checkedProgress > 0.5f) R.drawable.close else R.drawable.add),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.animateIcon(checkedProgress = { checkedProgress }),
                )
            }
        },
    ) {
        menuItems.forEach { (icon, label, action) ->
            FloatingActionButtonMenuItem(
                onClick = { onExpandedChange(false); action() },
                icon = {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                text = { Text(text = stringResource(id = label)) },
            )
        }
    }

    if (showUrlDialog) {
        SingleTextInputDialog(
            title = stringResource(id = R.string.enter_url_title),
            message = stringResource(id = R.string.enter_url_message),
            label = stringResource(id = R.string.url),
            confirmText = stringResource(id = R.string.import_action),
            onConfirm = { url ->
                onImportFromUrl(url)
                showUrlDialog = false
            },
            onDismiss = {
                showUrlDialog = false
            },
        )
    }
}
