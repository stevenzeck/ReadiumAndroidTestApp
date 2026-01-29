package com.example.readiumandroidtestapp.core.domain.gateway

import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset

interface AssetRetrieverGateway {
    suspend fun retrieve(url: AbsoluteUrl): Result<Asset>
}
