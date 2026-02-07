package com.example.readiumandroidtestapp.core.data.gateway

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.FileExtension
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.format.Format
import org.readium.r2.shared.util.format.FormatSpecification
import org.readium.r2.shared.util.format.Specification
import org.readium.r2.shared.util.mediatype.MediaType

class DefaultAssetRetrieverGatewayTest {

    private val assetRetriever: AssetRetriever = mockk()
    private val gateway = DefaultAssetRetrieverGateway(assetRetriever = assetRetriever)

    @Test
    fun `retrieve returns success when retriever succeeds`() = runTest {
        val url = mockk<AbsoluteUrl>()

        val format = Format(
            specification = FormatSpecification(Specification.Epub),
            mediaType = MediaType.EPUB,
            fileExtension = FileExtension(value = "epub"),
        )

        val asset = mockk<Asset>()
        every { asset.format } returns format

        coEvery { assetRetriever.retrieve(url = url) } returns Try.success(success = asset)

        val result = gateway.retrieve(url = url)

        assertTrue(result.isSuccess)
        assertEquals(asset, result.getOrNull())
        assertEquals(MediaType.EPUB, result.getOrNull()?.format?.mediaType)
    }

    @Test
    fun `retrieve returns failure when retriever fails`() = runTest {
        val url = mockk<AbsoluteUrl>()
        val error = mockk<AssetRetriever.RetrieveUrlError>()
        every { error.message } returns "Retrieval failed"

        coEvery { assetRetriever.retrieve(url = url) } returns Try.failure(failure = error)

        val result = gateway.retrieve(url = url)

        assertTrue(result.isFailure)
        assertEquals("Retrieval failed", result.exceptionOrNull()?.message)
    }
}
