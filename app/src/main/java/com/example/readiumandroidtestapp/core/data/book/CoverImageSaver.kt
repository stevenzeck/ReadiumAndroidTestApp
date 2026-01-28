package com.example.readiumandroidtestapp.core.data.book

import android.graphics.Bitmap
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.cover
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject

interface CoverImageSaver {
    suspend fun saveCover(publication: Publication): String?
}

class DefaultCoverImageSaver @Inject constructor(
    private val storageGateway: StorageGateway,
) : CoverImageSaver {

    override suspend fun saveCover(publication: Publication): String? {
        return try {
            val coverBitmap = publication.cover() ?: return null
            val coverFile = File(storageGateway.filesDir, "covers/${UUID.randomUUID()}.jpg")
            coverFile.parentFile?.mkdirs()

            FileOutputStream(coverFile).use { out ->
                coverBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            coverFile.absolutePath
        } catch (e: Exception) {
            Timber.w(t = e, message = "Failed to save cover image")
            null
        }
    }
}
