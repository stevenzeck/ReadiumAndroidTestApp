package com.example.readiumandroidtestapp.core.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.readiumandroidtestapp.core.ui.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class DataStoreSettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) : SettingsRepository {

    private val THEME_KEY = intPreferencesKey(name = "app_theme")

    override val appTheme: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeOrdinal = preferences[THEME_KEY] ?: AppTheme.SYSTEM.ordinal
            AppTheme.entries.toTypedArray().getOrElse(index = themeOrdinal) { AppTheme.SYSTEM }
        }

    override suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme.ordinal
        }
    }
}
