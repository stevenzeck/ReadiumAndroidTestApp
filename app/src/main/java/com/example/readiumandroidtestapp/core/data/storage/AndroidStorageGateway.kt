package com.example.readiumandroidtestapp.core.data.storage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import dagger.hilt.android.qualifiers.ApplicationContext
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.toUrl
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import kotlin.uuid.Uuid

/**
 * Implementation of [StorageGateway] that bridges the Android-specific context
 * and the domain layer's platform-agnostic requirements.
 *
 * This class abstracts away Android's [android.content.Context], [android.content.ContentResolver], and [android.net.Uri]
 * handling, allowing the domain layer to interact with storage using standard
 * types and interfaces.
 */
class AndroidStorageGateway @Inject constructor(
    @ApplicationContext private val context: Context,
) : StorageGateway {

    override val filesDir: File
        get() = context.filesDir

    override fun openInputStream(uri: Uri): InputStream? =
        context.contentResolver.openInputStream(uri)

    override fun resolveExtension(uri: Uri): String {
        if (ContentResolver.SCHEME_FILE == uri.scheme) {
            return MimeTypeMap.getFileExtensionFromUrl(uri.toString()) ?: "epub"
        }
        val mimeType = context.contentResolver.getType(uri)
        return if (mimeType != null) {
            resolveExtensionFromMimeType(mimeType) ?: "epub"
        } else {
            "epub"
        }
    }

    override fun resolveExtensionFromMimeType(mimeType: String): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }

    override fun toUrl(file: File): AbsoluteUrl {
        return file.toUrl(isDirectory = false)
    }

    override fun deleteFile(path: String): Boolean {
        return File(path).delete()
    }

    override fun saveFileFromStream(input: InputStream, extension: String?): Try<File, Exception> {
        return try {
            val safeExtension = when {
                extension == null -> ".epub"
                extension.startsWith(prefix = ".") -> extension
                else -> ".$extension"
            }
            val filename = "${Uuid.random()}$safeExtension"
            val file = File(filesDir, filename)

            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
            Try.success(success = file)
        } catch (e: Exception) {
            Try.failure(failure = e)
        }
    }
}
