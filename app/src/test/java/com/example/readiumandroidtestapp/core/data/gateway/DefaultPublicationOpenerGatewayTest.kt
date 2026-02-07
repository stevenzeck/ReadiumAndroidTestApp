package com.example.readiumandroidtestapp.core.data.gateway

import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.readium.r2.shared.publication.LocalizedString
import org.readium.r2.shared.publication.Metadata
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.asset.Asset
import org.readium.r2.streamer.PublicationOpener

class DefaultPublicationOpenerGatewayTest {

    private val publicationOpener: PublicationOpener = mockk()
    private val gateway = DefaultPublicationOpenerGateway(publicationOpener = publicationOpener)

    @Test
    fun `open returns success when opener succeeds`() = runTest {
        val asset = mockk<Asset>()
        val publication = mockk<Publication>()
        val metadata = Metadata(
            localizedTitle = LocalizedString(value = "Open Book"),
            identifier = "urn:uuid:12345",
        )
        every { publication.metadata } returns metadata

        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = true,
            )
        } returns Try.success(success = publication)

        val result = gateway.open(asset = asset, allowUserInteraction = true)

        assertTrue(result.isSuccess)
        val resultPub = result.getOrNull()
        assertEquals(publication, resultPub)
        assertEquals("Open Book", resultPub?.metadata?.title)
    }

    @Test
    fun `open returns failure when opener fails`() = runTest {
        val asset = mockk<Asset>()
        val error = mockk<PublicationOpener.OpenError>()
        every { error.message } returns "Opening failed"

        coEvery {
            publicationOpener.open(
                asset = asset,
                allowUserInteraction = false,
            )
        } returns Try.failure(failure = error)

        val result = gateway.open(asset = asset, allowUserInteraction = false)

        assertTrue(result.isFailure)
        assertEquals("Opening failed", result.exceptionOrNull()?.message)
    }
}
