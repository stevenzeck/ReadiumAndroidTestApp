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
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class CoverImageSaver @Inject constructor(
    private val storageGateway: StorageGateway,
    private val httpGateway: HttpGateway,
) {

    suspend fun saveCover(publication: Publication): String? {
        return try {
            val coverBitmap = publication.cover()
            val coverFile = File(storageGateway.filesDir, "covers/${Uuid.random()}.jpg")

            if (coverBitmap == null) {
                val link = publication.linkWithRel("cover")
                    ?: publication.manifest.links.firstOrNull { it.rels.contains("cover") }
                if (link != null) {
                    val resourceData = publication.get(link)?.read()?.getOrNull()
                    if (resourceData != null) {
                        coverFile.parentFile?.mkdirs()
                        FileOutputStream(coverFile).use { out ->
                            out.write(resourceData)
                        }
                        return coverFile.absolutePath
                    }

                    val coverLinkStr = link.href.toString()
                    val url = AbsoluteUrl(coverLinkStr)
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
