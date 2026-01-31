package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.domain.gateway.AssetRetrieverGateway
import com.example.readiumandroidtestapp.core.domain.gateway.PublicationOpenerGateway
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset

class OpenPublicationUseCaseTest {

    private val assetRetriever: AssetRetrieverGateway = mockk()
    private val publicationOpener: PublicationOpenerGateway = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val useCase = DefaultOpenPublicationUseCase(
        assetRetriever = assetRetriever,
        publicationOpener = publicationOpener,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `invoke returns success when asset and publication open successfully`() =
        runTest(context = testDispatcher) {
            val url = mockk<AbsoluteUrl>()
            val asset = mockk<Asset>()
            val publication = mockk<Publication>()

            coEvery { assetRetriever.retrieve(url = url) } returns Result.success(value = asset)
            coEvery {
                publicationOpener.open(
                    asset = asset,
                    allowUserInteraction = true,
                )
            } returns Result.success(value = publication)

            val result = useCase(url = url)

            assertTrue(result.isSuccess)
            assertEquals(publication, result.getOrNull()?.publication)
            assertEquals(asset, result.getOrNull()?.asset)
        }

    @Test
    fun `invoke returns failure when asset retrieval fails`() = runTest(context = testDispatcher) {
        val url = mockk<AbsoluteUrl>()
        val error = Exception("Asset retrieval failed")

        coEvery { assetRetriever.retrieve(url = url) } returns Result.failure(exception = error)

        val result = useCase(url = url)

        assertTrue(result.isFailure)
        assertEquals(error, result.exceptionOrNull())

        coVerify(exactly = 0) {
            publicationOpener.open(
                asset = any(),
                allowUserInteraction = any(),
            )
        }
    }

    @Test
    fun `invoke returns failure and closes asset when publication opening fails`() =
        runTest(context = testDispatcher) {
            val url = mockk<AbsoluteUrl>()
            val asset = mockk<Asset>(relaxed = true)
            val error = Exception("Publication opening failed")

            coEvery { assetRetriever.retrieve(url = url) } returns Result.success(value = asset)
            coEvery {
                publicationOpener.open(
                    asset = asset,
                    allowUserInteraction = true,
                )
            } returns Result.failure(exception = error)
            coEvery { asset.close() } returns Unit

            val result = useCase(url = url)

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())

            coVerify { asset.close() }
        }
}
