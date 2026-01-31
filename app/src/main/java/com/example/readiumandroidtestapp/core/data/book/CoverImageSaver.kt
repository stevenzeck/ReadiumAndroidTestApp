package com.example.readiumandroidtestapp.core.data.book

import org.readium.r2.shared.publication.Publication

interface CoverImageSaver {
    suspend fun saveCover(publication: Publication): String?
}
