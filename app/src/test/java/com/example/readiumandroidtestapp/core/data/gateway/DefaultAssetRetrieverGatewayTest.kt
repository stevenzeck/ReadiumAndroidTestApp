package com.example.readiumandroidtestapp.core.data.gateway

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.FileExtension
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.AssetRetriever
import org.readium.r2.shared.util.asset.ResourceAsset
import org.readium.r2.shared.util.format.Format
import org.readium.r2.shared.util.format.FormatSpecification
import org.readium.r2.shared.util.format.Specification
import org.readium.r2.shared.util.mediatype.MediaType
import org.readium.r2.shared.util.resource.Resource

@RunWith(AndroidJUnit4::class)
class DefaultAssetRetrieverGatewayTest {

    private val assetRetriever: AssetRetriever = mockk()
    private val gateway = DefaultAssetRetrieverGateway(assetRetriever = assetRetriever)

    @Test
    fun `retrieve returns success when retriever succeeds`() = runTest {
        val url = AbsoluteUrl(url = "http://example.com/book.epub")!!

        val format = Format(
            specification = FormatSpecification(Specification.Epub),
            mediaType = MediaType.EPUB,
            fileExtension = FileExtension(value = "epub"),
        )

        val resource = mockk<Resource>(relaxed = true)
        val realAsset = ResourceAsset(format = format, resource = resource)

        coEvery {
            assetRetriever.retrieve(url = any())
        } returns Try.success(success = realAsset)

        val result = gateway.retrieve(url)

        assertTrue(result.isSuccess)

        val returnedAsset = result.getOrNull()
        assertTrue("Expected ResourceAsset", returnedAsset is ResourceAsset)
        assertEquals(format, returnedAsset?.format)
        assertEquals(MediaType.EPUB, returnedAsset?.format?.mediaType)
    }

    @Test
    fun `retrieve returns failure when retriever fails`() = runTest {
        val url = AbsoluteUrl(url = "http://example.com/book.epub")!!

        val error = AssetRetriever.RetrieveUrlError.FormatNotSupported()

        coEvery {
            assetRetriever.retrieve(url = any())
        } returns Try.failure(failure = error)

        val result = gateway.retrieve(url)

        assertTrue(result.isFailure)
        assertEquals("Asset format is not supported.", result.exceptionOrNull()?.message)
    }
}
