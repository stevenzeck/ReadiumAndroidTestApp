package com.example.readiumandroidtestapp.features.reader.ui.search

import androidx.paging.PagingData
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.readium.r2.shared.publication.Publication
import org.readium.navigator.common.Decoration as NewDecoration
import org.readium.r2.navigator.Decoration as LegacyDecoration

interface ReaderSearchManager {
    val searchQuery: StateFlow<String?>
    val pdfSearchDecorations: Flow<List<LegacyDecoration>>

    fun epubSearchDecorations(isFixedLayout: Boolean): Flow<List<NewDecoration<*>>>

    fun onSearchQueryChanged(query: String)
    fun getSearchResults(
        publicationFlow: Flow<Publication?>,
        scope: CoroutineScope,
    ): Flow<PagingData<SearchItem>>
}
