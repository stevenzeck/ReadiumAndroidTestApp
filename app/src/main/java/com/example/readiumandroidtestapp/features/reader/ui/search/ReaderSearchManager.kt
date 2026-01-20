package com.example.readiumandroidtestapp.features.reader.ui.search

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.insertSeparators
import androidx.paging.map
import com.example.readiumandroidtestapp.features.reader.data.SearchPagingSource
import com.example.readiumandroidtestapp.features.reader.ui.state.SearchItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.readium.r2.navigator.Decoration
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.search.search
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ReaderSearchManager @Inject constructor() {

    private val _searchQuery = MutableStateFlow<String?>(value = null)
    val searchQuery: StateFlow<String?> = _searchQuery.asStateFlow()

    private val _searchLocators = MutableStateFlow<List<Locator>>(value = emptyList())

    val searchDecorations: Flow<List<Decoration>> = _searchLocators.map { locators ->
        locators.mapIndexed { index, locator ->
            Decoration(
                id = "search-$index",
                locator = locator,
                style = Decoration.Style.Underline(tint = Color.Red.toArgb()),
            )
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getSearchResults(
        publicationFlow: Flow<Publication?>,
        scope: CoroutineScope,
    ): Flow<PagingData<SearchItem>> {
        return combine(
            _searchQuery.debounce(timeoutMillis = 300),
            publicationFlow,
        ) { query, pub ->
            Pair(query, pub)
        }.flatMapLatest { (query, pub) ->
            _searchLocators.value = emptyList()

            if (query.isNullOrBlank() || pub == null) {
                flowOf(value = PagingData.empty())
            } else {
                val iterator = pub.search(query)

                if (iterator == null) {
                    flowOf(value = PagingData.empty())
                } else {
                    Pager(config = PagingConfig(pageSize = 20)) {
                        SearchPagingSource(iterator) { locators ->
                            _searchLocators.update { it + locators }
                        }
                    }.flow.map { pagingData ->
                        pagingData
                            .map { SearchItem.Result(locator = it) }
                            .insertSeparators { before: SearchItem.Result?, after: SearchItem.Result? ->
                                val beforeTitle = before?.locator?.title
                                val afterTitle = after?.locator?.title
                                if (afterTitle != null && beforeTitle != afterTitle) {
                                    SearchItem.Header(afterTitle)
                                } else {
                                    null
                                }
                            }
                    }
                }
            }
        }.cachedIn(scope)
    }
}
