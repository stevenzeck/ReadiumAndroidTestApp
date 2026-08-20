package com.example.readiumandroidtestapp.core.data.book

import android.graphics.Bitmap
import com.example.readiumandroidtestapp.core.data.di.IoDispatcher
import com.example.readiumandroidtestapp.core.data.storage.StorageManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.readium.r2.shared.publication.Publication
import org.readium.r2.shared.publication.services.cover
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.http.HttpClient
import org.readium.r2.shared.util.http.HttpRequest
import org.readium.r2.shared.util.http.fetch
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class CoverImageSaver @Inject constructor(
    private val storageManager: StorageManager,
    private val httpClient: HttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun saveCover(publication: Publication): String? = withContext(context = ioDispatcher) {
        try {
            val coverBitmap = publication.cover()
            val coverFile = File(storageManager.filesDir, "covers/${Uuid.random()}.jpg")

            if (coverBitmap == null) {
                val link = publication.linkWithRel(rel = "cover")
                    ?: publication.manifest.links.firstOrNull { it.rels.contains(element = "cover") }
                if (link != null) {
                    val resourceData = publication.get(link = link)?.read()?.getOrNull()
                    if (resourceData != null) {
                        coverFile.parentFile?.mkdirs()
                        FileOutputStream(coverFile).use { out ->
                            out.write(resourceData)
                        }
                        return@withContext coverFile.absolutePath
                    }

                    val coverLinkStr = link.href.toString()
                    val url = AbsoluteUrl(url = coverLinkStr)
                    if (url != null) {
                        val response =
                            httpClient.fetch(request = HttpRequest(url = url)).getOrNull()
                        if (response != null) {
                            coverFile.parentFile?.mkdirs()
                            FileOutputStream(coverFile).use { out ->
                                out.write(response.body)
                            }
                            return@withContext coverFile.absolutePath
                        }
                    }
                }
                return@withContext null
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
