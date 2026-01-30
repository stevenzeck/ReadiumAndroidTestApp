package com.example.readiumandroidtestapp.core.data.gateway

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.shared.util.asset.AssetRetriever

class DefaultAssetRetrieverGatewayTest {

    private val assetRetriever: AssetRetriever = mockk()
    private val gateway = DefaultAssetRetrieverGateway(assetRetriever = assetRetriever)

    @Test
    fun `retrieve returns success when retriever succeeds`() = runTest {
        val url = mockk<AbsoluteUrl>()
        val asset = mockk<Asset>()

        coEvery { assetRetriever.retrieve(url = url) } returns Try.success(success = asset)

        val result = gateway.retrieve(url = url)

        assertTrue(result.isSuccess)
        assertEquals(asset, result.getOrNull())
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
