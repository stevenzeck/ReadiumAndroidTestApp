package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.data.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.streamer.PublicationOpener
import javax.inject.Inject
import javax.inject.Singleton

data class OpenedBook(
    val publication: Publication,
    val asset: Asset,
)

@Singleton
class OpenPublicationUseCase @Inject constructor(
    private val assetRetriever: AssetRetriever,
    private val publicationOpener: PublicationOpener,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {
    suspend operator fun invoke(url: AbsoluteUrl): Result<OpenedBook> =
        withContext(context = ioDispatcher) {
            val assetResult = assetRetriever.retrieve(url = url)

            assetResult.fold(
                onSuccess = { asset ->
                    val openResult = publicationOpener.open(
                        asset = asset,
                        allowUserInteraction = true,
                    )

                    openResult.fold(
                        onSuccess = { publication ->
                            Result.success(
                                value = OpenedBook(
                                    publication = publication,
                                    asset = asset,
                                ),
                            )
                        },
                        onFailure = { error ->
                            asset.close()
                            Result.failure(exception = Exception(error.message))
                        },
                    )
                },
                onFailure = { error ->
                    Result.failure(exception = Exception(error.message))
                },
            )
        }
}
