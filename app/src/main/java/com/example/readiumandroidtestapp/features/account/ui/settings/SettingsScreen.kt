package com.example.readiumandroidtestapp.features.account.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.designsystem.components.ReadiumScaffold
import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme

@Composable
fun SettingsScreen(
    showBackButton: Boolean = false,
    onNavigateBack: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val theme by viewModel.appTheme.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    ReadiumScaffold(
        title = stringResource(id = R.string.settings),
        scrollBehavior = scrollBehavior,
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
        navigationIcon = {
            if (showBackButton) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.arrow_back),
                        contentDescription = stringResource(id = R.string.back),
                    )
                }
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp),
        ) {
            Text(
                text = "Theme",
                style = MaterialTheme.typography.titleMedium,
            )

            Column {
                ThemeOption(
                    label = stringResource(id = R.string.system_default),
                    selected = theme == AppTheme.SYSTEM,
                    onClick = { viewModel.setTheme(AppTheme.SYSTEM) },
                )
                ThemeOption(
                    label = stringResource(id = R.string.light),
                    selected = theme == AppTheme.LIGHT,
                    onClick = { viewModel.setTheme(AppTheme.LIGHT) },
                )
                ThemeOption(
                    label = stringResource(id = R.string.dark),
                    selected = theme == AppTheme.DARK,
                    onClick = { viewModel.setTheme(AppTheme.DARK) },
                )
            }
        }
    }
}

@Composable
fun ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick,
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 8.dp),
        )
    }
}
