package com.example.readiumandroidtestapp.core.data.settings

import com.example.readiumandroidtestapp.core.ui.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val appTheme: Flow<AppTheme>
    suspend fun setAppTheme(theme: AppTheme)
}
