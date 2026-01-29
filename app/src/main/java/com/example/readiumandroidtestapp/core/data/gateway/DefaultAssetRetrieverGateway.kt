package com.example.readiumandroidtestapp.core.data.gateway

import com.example.readiumandroidtestapp.core.domain.gateway.AssetRetrieverGateway
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import javax.inject.Inject

class DefaultAssetRetrieverGateway @Inject constructor(
    private val assetRetriever: AssetRetriever
) : AssetRetrieverGateway {

    override suspend fun retrieve(url: AbsoluteUrl): Result<Asset> {
        return assetRetriever.retrieve(url).fold(
            onSuccess = { Result.success(value = it) },
            onFailure = { Result.failure(Exception(it.message)) }
        )
    }
}
