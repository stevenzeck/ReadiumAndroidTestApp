package com.example.readiumandroidtestapp.features.catalogs.ui.feed

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.data.database.CatalogDao
import com.example.readiumandroidtestapp.core.domain.model.Catalog
import com.example.readiumandroidtestapp.core.domain.opds.OpdsParser
import com.example.readiumandroidtestapp.core.utils.UserMessageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CatalogFeedViewModel @Inject constructor(
    private val catalogDao: CatalogDao,
    private val userMessageManager: UserMessageManager,
    private val opdsParser: OpdsParser,
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
            opdsParser.parseUrlString(url = url).onSuccess { parseData ->
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
}

@Immutable
sealed interface CatalogFeedUiState {
    data object Loading : CatalogFeedUiState

    @Immutable
    data class Success(val catalogs: List<Catalog>) : CatalogFeedUiState

    data object Error : CatalogFeedUiState
}
