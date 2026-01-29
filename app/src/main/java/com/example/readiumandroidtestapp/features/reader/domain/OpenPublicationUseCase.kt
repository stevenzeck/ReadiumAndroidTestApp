package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.data.di.IoDispatcher
import com.example.readiumandroidtestapp.core.domain.gateway.AssetRetrieverGateway
import com.example.readiumandroidtestapp.core.domain.gateway.PublicationOpenerGateway
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset
import javax.inject.Inject

data class OpenedBook(
    val publication: Publication,
    val asset: Asset,
)

class OpenPublicationUseCase @Inject constructor(
    private val assetRetriever: AssetRetrieverGateway,
    private val publicationOpener: PublicationOpenerGateway,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(url: AbsoluteUrl): Result<OpenedBook> =
        withContext(context = ioDispatcher) {

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
                            Result.failure(exception = error)
                        },
                    )
                },
                onFailure = { error ->
                    Result.failure(exception = error)
                },
            )
        }
}
