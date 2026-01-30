package com.example.readiumandroidtestapp.features.reader.domain

import org.readium.r2.shared.util.AbsoluteUrl

interface OpenPublicationUseCase {
    suspend operator fun invoke(url: AbsoluteUrl): Result<OpenedBook>
}
