package com.example.readiumandroidtestapp.features.reader.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.readium.r2.shared.publication.Locator
import org.readium.navigator.common.Decoration as NewDecoration
import org.readium.r2.navigator.Decoration as LegacyDecoration

interface ReaderDecorationManager {
    val showHighlightDialog: StateFlow<Boolean>

    fun pdfDecorationFlow(bookId: Long?): Flow<List<LegacyDecoration>>

    fun epubDecorationFlow(
        bookId: Long?,
        isFixedLayout: Boolean,
    ): Flow<List<NewDecoration<*>>>

    fun onHighlightAction(selection: Locator)
    fun dismissHighlightDialog()
    suspend fun saveHighlight(bookId: Long, note: String, color: Int)
}
