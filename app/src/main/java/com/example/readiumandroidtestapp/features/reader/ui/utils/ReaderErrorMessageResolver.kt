package com.example.readiumandroidtestapp.features.reader.ui.utils

import com.example.readiumandroidtestapp.R
import com.example.readiumandroidtestapp.core.designsystem.utils.UiText
import com.example.readiumandroidtestapp.features.reader.ui.state.ReaderError

object ReaderErrorMessageResolver {

    fun resolve(error: ReaderError): UiText {
        val unknownError = UiText.StringResource(resId = R.string.unknown_error)

        return when (error) {
            is ReaderError.InvalidBookLocation -> UiText.StringResource(resId = R.string.invalid_book_location)

            is ReaderError.PublicationOpenFailed -> {
                val arg =
                    error.cause.message?.let { UiText.DynamicString(value = it) } ?: unknownError
                UiText.StringResource(
                    resId = R.string.failed_publication_opening,
                    args = listOf(arg),
                )
            }

            is ReaderError.AssetRetrievalFailed -> {
                val arg =
                    error.cause.message?.let { UiText.DynamicString(value = it) } ?: unknownError
                UiText.StringResource(
                    resId = R.string.failed_asset_retrieval,
                    args = listOf(arg),
                )
            }

            is ReaderError.NavigatorCreationFailed -> {
                val arg =
                    error.cause.message?.let { UiText.DynamicString(value = it) } ?: unknownError
                UiText.StringResource(
                    resId = R.string.failed_create_audio_navigator,
                    args = listOf(arg),
                )
            }
        }
    }
}
