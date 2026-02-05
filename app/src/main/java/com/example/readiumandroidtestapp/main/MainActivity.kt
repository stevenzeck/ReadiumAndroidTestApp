package com.example.readiumandroidtestapp.main

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme
import com.example.readiumandroidtestapp.core.navigation.route.NavEntryBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * The single Activity for the application.
 *
 * Responsibilities:
 * 1. **Entry Point**: Sets up the Compose content view.
 * 2. **Dependency Injection Root**: Injects the [NavEntryBuilder] set (Multibindings) to pass to [ReadiumApp].
 * 3. **Theme Management**: Observes the app theme preference and applies the [AppTheme].
 */
@AndroidEntryPoint
@ExperimentalFoundationApi
class MainActivity : AppCompatActivity() {

    /**
     * The set of navigation entry builders collected from all Hilt modules.
     * This is the mechanism by which features "plug in" to the main app.
     */
    @Inject
    lateinit var entryBuilders: Set<@JvmSuppressWildcards NavEntryBuilder>

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState = savedInstanceState)

        enableEdgeToEdge()

        setContent {
            val appTheme by viewModel.appTheme.collectAsStateWithLifecycle()

            val useDarkTheme = when (appTheme) {
                AppTheme.SYSTEM -> isSystemInDarkTheme()
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
            }

            AppTheme(useDarkTheme = useDarkTheme) {
                ReadiumApp(
                    entryBuilders = entryBuilders,
                )
            }
        }
    }
}
