package com.example.readiumandroidtestapp.core.domain.repository

import com.example.readiumandroidtestapp.core.designsystem.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val appTheme: Flow<AppTheme>
    suspend fun setAppTheme(theme: AppTheme)
}
