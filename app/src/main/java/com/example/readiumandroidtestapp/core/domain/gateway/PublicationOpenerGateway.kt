package com.example.readiumandroidtestapp.core.domain.gateway

import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.Asset

interface PublicationOpenerGateway {
    suspend fun open(asset: Asset, allowUserInteraction: Boolean): Result<Publication>
}
