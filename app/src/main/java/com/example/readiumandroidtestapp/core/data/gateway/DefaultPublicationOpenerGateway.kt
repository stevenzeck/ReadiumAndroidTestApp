package com.example.readiumandroidtestapp.core.data.gateway

import com.example.readiumandroidtestapp.core.domain.gateway.PublicationOpenerGateway
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.streamer.PublicationOpener
import javax.inject.Inject

class DefaultPublicationOpenerGateway @Inject constructor(
    private val publicationOpener: PublicationOpener
) : PublicationOpenerGateway {

    override suspend fun open(asset: Asset, allowUserInteraction: Boolean): Result<Publication> {
        return publicationOpener.open(
            asset = asset,
            allowUserInteraction = allowUserInteraction
        ).fold(
            onSuccess = { Result.success(value = it) },
            onFailure = { Result.failure(Exception(it.message)) }
        )
    }
}
