package com.example.readiumandroidtestapp.core.data.book

import android.graphics.Bitmap
import com.example.readiumandroidtestapp.core.domain.network.HttpGateway
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.cover
import org.readium.r2.shared.util.AbsoluteUrl
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import kotlin.uuid.Uuid

class DefaultCoverImageSaver @Inject constructor(
    private val storageGateway: StorageGateway,
    private val httpGateway: HttpGateway,
) : CoverImageSaver {

    override suspend fun saveCover(publication: Publication): String? {
        return try {
            val coverBitmap = publication.cover()
            val coverFile = File(storageGateway.filesDir, "covers/${Uuid.random()}.jpg")
            
            if (coverBitmap == null) {
                val coverLink = publication.linkWithRel("cover")?.href?.toString() ?: publication.manifest.links.firstOrNull { it.rels.contains("cover") }?.href?.toString()
                if (coverLink != null) {
                    val url = AbsoluteUrl(coverLink)
                    if (url != null) {
                        val result = httpGateway.fetch(url).getOrNull()
                        if (result != null) {
                            coverFile.parentFile?.mkdirs()
                            FileOutputStream(coverFile).use { out ->
                                out.write(result.body)
                            }
                            return coverFile.absolutePath
                        }
                    }
                }
                return null
            }

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
