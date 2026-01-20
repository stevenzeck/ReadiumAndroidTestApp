package com.example.readiumandroidtestapp.features.reader.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.streamer.PublicationOpener
import javax.inject.Inject

data class OpenedBook(
    val publication: Publication,
    val asset: Asset,
)

class OpenPublicationUseCase @Inject constructor(
    private val assetRetriever: AssetRetriever,
    private val publicationOpener: PublicationOpener,
) {
    suspend operator fun invoke(url: AbsoluteUrl): Result<OpenedBook> =
        withContext(context = Dispatchers.IO) {

            val assetResult = assetRetriever.retrieve(url)

            return@withContext assetResult.fold(
                onSuccess = { asset ->
                    val openResult = publicationOpener.open(
                        asset = asset,
                        allowUserInteraction = true,
                    )

                    openResult.fold(
                        onSuccess = { publication ->
                            Result.success(value = OpenedBook(publication, asset))
                        },
                        onFailure = { error ->
                            asset.close()
                            Result.failure(Exception(error.message))
                        },
                    )
                },
                onFailure = { error ->
                    Result.failure(Exception(error.message))
                },
            )
        }
}
