package com.example.readiumandroidtestapp.features.reader.ui.state

data class ReaderActions(
    val onNavigateBack: () -> Unit,
    val onSearchClick: () -> Unit,
    val onTtsClick: () -> Unit,
    val onSettingsClick: () -> Unit,
    val onTocClick: () -> Unit,
    val onTtsPlayPause: () -> Unit,
    val onTtsPrevious: () -> Unit,
    val onTtsNext: () -> Unit,
    val onTtsStop: () -> Unit,
)
