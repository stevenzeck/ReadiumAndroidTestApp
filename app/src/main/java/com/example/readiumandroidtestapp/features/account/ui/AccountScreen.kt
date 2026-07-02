package com.example.readiumandroidtestapp.features.account.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.NavigableListDetailPaneScaffold
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.designsystem.components.ReadiumScaffold
import com.example.readiumandroidtestapp.core.navigation.route.AccountScreens
import com.example.readiumandroidtestapp.features.account.ui.settings.SettingsScreen
import kotlinx.coroutines.launch

@Composable
fun AccountScreen(
    settingsScreen: @Composable () -> Unit = { SettingsScreen() },
    aboutScreen: @Composable () -> Unit = { AboutInfoScreen() },
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<AccountScreens>()
    val scope = rememberCoroutineScope()

    BackHandler(enabled = navigator.canNavigateBack()) {
        scope.launch { navigator.navigateBack() }
    }

    NavigableListDetailPaneScaffold(
        navigator = navigator,
        listPane = {
            AnimatedPane {
                AccountListPane(
                    onItemClick = { detail ->
                        scope.launch {
                            navigator.navigateTo(
                                pane = ListDetailPaneScaffoldRole.Detail,
                                contentKey = detail,
                            )
                        }
                    },
                )
            }
        },
        detailPane = {
            AnimatedPane {
                val detail = navigator.currentDestination?.contentKey
                if (detail != null) {
                    when (detail) {
                        AccountScreens.Settings -> settingsScreen()
                        AccountScreens.About -> aboutScreen()
                    }
                }
            }
        },
    )
}

@Composable
fun AccountListPane(onItemClick: (AccountScreens) -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    ReadiumScaffold(
        title = stringResource(id = R.string.account),
        scrollBehavior = scrollBehavior,
        modifier = Modifier.nestedScroll(connection = scrollBehavior.nestedScrollConnection),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            ListItem(
                modifier = Modifier.clickable { onItemClick(AccountScreens.Settings) },
                content = { Text(text = stringResource(id = R.string.settings)) },
            )
            HorizontalDivider()
            ListItem(
                modifier = Modifier.clickable { onItemClick(AccountScreens.About) },
                content = { Text(text = stringResource(id = R.string.about)) },
            )
        }
    }
}
