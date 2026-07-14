package com.example.readiumandroidtestapp.features.catalogs.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.domain.opds.OpdsParser
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.readium.r2.shared.opds.Feed

@HiltViewModel(assistedFactory = CatalogDetailViewModel.Factory::class)
class CatalogDetailViewModel @AssistedInject constructor(
    @Assisted private val catalog: Catalog,
    private val opdsParser: OpdsParser,
) : ViewModel() {
    val feedState: StateFlow<FeedState> field = MutableStateFlow<FeedState>(value = FeedState.Loading)

    init {
        fetchFeed()
    }

    /**
     * Fetches the feed content. Optimized to use the persisted type
     * to call the correct parser directly via OpdsParser.
     */
    fun fetchFeed() {
        viewModelScope.launch {
            feedState.value = FeedState.Loading

            opdsParser.parseUrlString(url = catalog.href, type = catalog.type)
                .onSuccess { parseData ->
                    feedState.value = FeedState.Success(feed = parseData.feed)
                }.onFailure { error ->
                    feedState.value = FeedState.Error(message = error.localizedMessage)
                }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(catalog: Catalog): CatalogDetailViewModel
    }
}

sealed interface FeedState {
    data object Loading : FeedState

    data class Success(val feed: Feed?) : FeedState

    data class Error(val message: String?) : FeedState
}
