package com.example.readiumandroidtestapp.features.reader.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.gateway.AssetRetrieverGateway
import com.example.readiumandroidtestapp.core.domain.gateway.PublicationOpenerGateway
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.asset.Asset

@RunWith(AndroidJUnit4::class)
class DefaultOpenPublicationUseCaseTest {

    private val assetRetriever: AssetRetrieverGateway = mockk()
    private val publicationOpener: PublicationOpenerGateway = mockk()
    private val testDispatcher = StandardTestDispatcher()
    private val useCase = OpenPublicationUseCase(
        assetRetriever = assetRetriever,
        publicationOpener = publicationOpener,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun `invoke returns OpenedBook on success`() = runTest(context = testDispatcher) {
        val url = AbsoluteUrl(url = "http://example.com/book.epub")!!
        val asset = mockk<Asset>(relaxed = true)
        val publication = mockk<Publication>(relaxed = true)

        coEvery { assetRetriever.retrieve(url = url) } returns Result.success(value = asset)
        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = true,
            )
        } returns Result.success(value = publication)

        val result = useCase(url)

        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.publication == publication)
        assertTrue(result.getOrNull()?.asset == asset)
    }

    @Test
    fun `invoke returns failure when asset retrieval fails`() = runTest(context = testDispatcher) {
        val url = mockk<AbsoluteUrl>()
        coEvery { assetRetriever.retrieve(url = url) } returns Result.failure(Exception("Failed"))

        val result = useCase(url = url)

        assertTrue(result.isFailure)
    }

    @Test
    fun `invoke returns failure and closes asset when opener fails`() =
        runTest(context = testDispatcher) {
            val url = mockk<AbsoluteUrl>()
            val asset = mockk<Asset>(relaxed = true)
            coEvery { assetRetriever.retrieve(url = url) } returns Result.success(value = asset)
            coEvery {
                publicationOpener.open(
                    asset = asset,
                    allowUserInteraction = true,
                )
            } returns Result.failure(Exception("Failed"))

            val result = useCase(url = url)

            assertTrue(result.isFailure)
            verify { asset.close() }
        }
}
