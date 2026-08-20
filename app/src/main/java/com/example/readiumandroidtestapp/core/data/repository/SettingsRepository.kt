package com.example.readiumandroidtestapp.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val themeKey = intPreferencesKey(name = "app_theme")

    val appTheme: Flow<AppTheme> = context.dataStore.data
        .map { preferences ->
            val themeOrdinal = preferences[themeKey] ?: AppTheme.SYSTEM.ordinal
            AppTheme.entries.toTypedArray().getOrElse(index = themeOrdinal) { AppTheme.SYSTEM }
        }

    suspend fun setAppTheme(theme: AppTheme) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.ordinal
        }
    }
}
