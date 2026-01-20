package com.example.readiumandroidtestapp.features.catalogs.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.data.database.CatalogDao
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.readium.r2.opds.OPDS1Parser
import org.readium.r2.opds.OPDS2Parser
import org.readium.r2.shared.opds.ParseData
import org.readium.r2.shared.util.Try
import javax.inject.Inject

@HiltViewModel
class CatalogFeedViewModel @Inject constructor(
    private val catalogDao: CatalogDao,
    private val userMessageManager: UserMessageManager,
) : ViewModel() {

    val catalogsState: StateFlow<CatalogFeedUiState> = catalogDao.getCatalogModels()
        .map<List<Catalog>, CatalogFeedUiState> { CatalogFeedUiState.Success(catalogs = it) }
        .catch { emit(value = CatalogFeedUiState.Error) }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
            initialValue = CatalogFeedUiState.Loading,
        )

    /**
     * Adds a new catalog to the database after validating the URL is a valid OPDS feed.
     */
    fun addCatalog(title: String, url: String) {
        viewModelScope.launch {
            parseFeed(url = url).onSuccess { parseData ->
                catalogDao.insertCatalog(
                    catalog = Catalog(
                        title = title,
                        href = url,
                        type = parseData.type,
                    ),
                )
            }.onFailure {
                userMessageManager.emitMessage(messageId = R.string.error_invalid_opds_feed)
            }
        }
    }

    fun deleteCatalog(catalog: Catalog) {
        viewModelScope.launch {
            catalog.id?.let { catalogDao.deleteCatalog(id = it) }
        }
    }

    fun editCatalog(catalog: Catalog, newTitle: String) {
        viewModelScope.launch {
            catalogDao.insertCatalog(
                catalog = catalog.copy(title = newTitle),
            )
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

sealed interface CatalogFeedUiState {
    data object Loading : CatalogFeedUiState
    data class Success(val catalogs: List<Catalog>) : CatalogFeedUiState
    data object Error : CatalogFeedUiState
}
