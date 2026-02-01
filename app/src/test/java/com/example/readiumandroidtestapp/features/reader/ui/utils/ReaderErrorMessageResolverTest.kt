package com.example.readiumandroidtestapp.features.reader.ui.utils

import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.ui.utils.UiText
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderError
import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderErrorMessageResolverTest {

    @Test
    fun `resolve InvalidBookLocation returns correct resource`() {
        val error = ReaderError.InvalidBookLocation
        val result = ReaderErrorMessageResolver.resolve(error)

        assertEquals(UiText.StringResource(resId = R.string.invalid_book_location), result)
    }

    @Test
    fun `resolve PublicationOpenFailed with message returns formatted resource`() {
        val msg = "File not found"
        val error = ReaderError.PublicationOpenFailed(cause = Exception(msg))
        val result = ReaderErrorMessageResolver.resolve(error)

        val expected = UiText.StringResource(
            resId = R.string.failed_publication_opening,
            args = listOf(UiText.DynamicString(value = msg)),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `resolve PublicationOpenFailed without message returns default error`() {
        val error = ReaderError.PublicationOpenFailed(cause = Exception())
        val result = ReaderErrorMessageResolver.resolve(error)

        val expected = UiText.StringResource(
            resId = R.string.failed_publication_opening,
            args = listOf(UiText.StringResource(resId = R.string.unknown_error)),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `resolve AssetRetrievalFailed returns formatted resource`() {
        val msg = "Network error"
        val error = ReaderError.AssetRetrievalFailed(cause = Throwable(msg))
        val result = ReaderErrorMessageResolver.resolve(error)

        val expected = UiText.StringResource(
            resId = R.string.failed_asset_retrieval,
            args = listOf(UiText.DynamicString(value = msg)),
        )
        assertEquals(expected, result)
    }

    @Test
    fun `resolve NavigatorCreationFailed returns formatted resource`() {
        val msg = "Navigator error"
        val error = ReaderError.NavigatorCreationFailed(cause = Throwable(msg))
        val result = ReaderErrorMessageResolver.resolve(error)

        val expected = UiText.StringResource(
            resId = R.string.failed_create_audio_navigator,
            args = listOf(UiText.DynamicString(value = msg)),
        )
        assertEquals(expected, result)
    }
}
