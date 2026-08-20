package com.example.readiumandroidtestapp.features.reader.domain

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
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.streamer.PublicationOpener

class OpenPublicationUseCaseTest {

    private val assetRetriever: AssetRetriever = mockk()
    private val publicationOpener: PublicationOpener = mockk()
    private val testDispatcher = StandardTestDispatcher()

    private val useCase = OpenPublicationUseCase(
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

            coEvery { assetRetriever.retrieve(url = url) } returns Try.success(success = asset)
            coEvery {
                publicationOpener.open(
                    asset = asset,
                    allowUserInteraction = true,
                )
            } returns Try.success(success = publication)

            val result = useCase(url = url)

            assertTrue(result.isSuccess)
            assertEquals(publication, result.getOrNull()?.publication)
            assertEquals(asset, result.getOrNull()?.asset)
        }

    @Test
    fun `invoke returns failure when asset retrieval fails`() = runTest(context = testDispatcher) {
        val url = mockk<AbsoluteUrl>()
        val error = AssetRetriever.RetrieveUrlError.FormatNotSupported()

        coEvery { assetRetriever.retrieve(url = url) } returns Try.failure(failure = error)

        val result = useCase(url = url)

        assertTrue(result.isFailure)
        assertEquals(error.message, result.exceptionOrNull()?.message)

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
            val error = PublicationOpener.OpenError.FormatNotSupported()

            coEvery { assetRetriever.retrieve(url = url) } returns Try.success(success = asset)
            coEvery {
                publicationOpener.open(
                    asset = asset,
                    allowUserInteraction = true,
                )
            } returns Try.failure(failure = error)
            coEvery { asset.close() } returns Unit

            val result = useCase(url = url)

            assertTrue(result.isFailure)
            assertEquals(error.message, result.exceptionOrNull()?.message)

            coVerify { asset.close() }
        }
}
