package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.readium.r2.navigator.Decoration
import org.readium.r2.shared.publication.Locator

interface ReaderDecorationManager {
    val showAnnotationDialog: StateFlow<Boolean>

    fun decorationFlow(bookId: Long?): Flow<List<Decoration>>
    fun onAnnotateAction(selection: Locator)
    fun dismissAnnotationDialog()
    suspend fun saveAnnotation(
        bookId: Long,
        note: String,
        color: Int,
        style: ReaderAnnotation.Style,
    )
}
