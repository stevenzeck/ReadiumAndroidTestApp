package com.example.readiumandroidtestapp.core.data.database

import com.example.readiumandroidtestapp.core.domain.model.Catalog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeCatalogDao : CatalogDao {

    private val catalogs = MutableStateFlow<Map<Long, Catalog>>(emptyMap())

    override suspend fun insertCatalog(catalog: Catalog): Long {
        val id =
            if (catalog.id == null || catalog.id == 0L) (catalogs.value.keys.maxOrNull() ?: 0L) + 1
            else catalog.id

        val newCatalog = catalog.copy(id = id)
        catalogs.update { it + (id to newCatalog) }
        return id
    }

    override fun getCatalogModels(title: String, href: String, type: Int): Flow<List<Catalog>> {
        return catalogs.map { map ->
            map.values.filter { it.title == title && it.href == href && it.type == type }
        }
    }

    override fun getCatalogModels(): Flow<List<Catalog>> {
        return catalogs.map { it.values.toList() }
    }

    override suspend fun deleteCatalog(id: Long) {
        catalogs.update { it - id }
    }
}
