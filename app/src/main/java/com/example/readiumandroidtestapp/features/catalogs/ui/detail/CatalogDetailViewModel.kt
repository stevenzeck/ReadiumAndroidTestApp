package com.example.readiumandroidtestapp.features.catalogs.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.domain.model.Catalog.Companion.TYPE_OPDS_1
import com.example.readiumandroidtestapp.core.domain.model.Catalog.Companion.TYPE_OPDS_2
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.readium.r2.opds.OPDS1Parser
import org.readium.r2.opds.OPDS2Parser
import org.readium.r2.shared.opds.Feed
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try
import javax.inject.Inject

@HiltViewModel
class CatalogDetailViewModel @Inject constructor(

) : ViewModel() {
    private val _feedState = MutableStateFlow<FeedState>(value = FeedState.Loading)
    val feedState: StateFlow<FeedState> = _feedState.asStateFlow()

    /**
     * Fetches the feed content. Optimized to use the persisted type
     * to call the correct parser directly.
     */
    fun fetchFeed(catalog: Catalog) {
        viewModelScope.launch {
            _feedState.value = FeedState.Loading

            val parseResult = when (catalog.type) {
                TYPE_OPDS_1 -> OPDS1Parser.parseUrlString(url = catalog.href)
                TYPE_OPDS_2 -> OPDS2Parser.parseUrlString(url = catalog.href)
                else -> parseFeed(url = catalog.href) // Fallback for unknown types
            }

            parseResult.onSuccess { parseData ->
                _feedState.value = FeedState.Success(feed = parseData.feed)
            }.onFailure { error ->
                _feedState.value = FeedState.Error(message = error.localizedMessage)
            }
        }
    }

    /**
     * Attempts to parse a URL using OPDS 2.0 logic first, falling back to OPDS 1.x.
     * Used for initial validation when adding new catalogs.
     */
    private suspend fun parseFeed(url: String): Try<ParseData, Exception> {
        return OPDS2Parser.parseUrlString(url = url).onFailure {
            return OPDS1Parser.parseUrlString(url = url)
        }
    }
}

sealed interface FeedState {
    data object Loading : FeedState
    data class Success(val feed: Feed?) : FeedState
    data class Error(val message: String?) : FeedState
}
