package com.example.readiumandroidtestapp.features.reader.ui.search

import androidx.paging.PagingData
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import org.readium.r2.navigator.Decoration
import org.readium.r2.shared.publication.Publication

interface ReaderSearchManager {
    val searchQuery: StateFlow<String?>
    val searchDecorations: Flow<List<Decoration>>

    fun onSearchQueryChanged(query: String)
    fun getSearchResults(
        publicationFlow: Flow<Publication?>,
        scope: CoroutineScope,
    ): Flow<PagingData<SearchItem>>
}
