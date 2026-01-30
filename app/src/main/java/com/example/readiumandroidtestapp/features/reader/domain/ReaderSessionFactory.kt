package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.domain.model.Book
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderUiState
import org.readium.r2.shared.publication.Publication

interface ReaderSessionFactory {
    suspend fun createVisualSession(
        book: Book,
        publication: Publication,
    ): ReaderUiState.Visual

    suspend fun createAudioSession(
        book: Book,
        publication: Publication,
    ): Result<ReaderUiState.Audio>
}
