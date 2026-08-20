package com.example.readiumandroidtestapp.core.data.storage

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.toUrl
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.uuid.Uuid

@Singleton
class StorageManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    val filesDir: File
        get() = context.filesDir

    fun openInputStream(uri: Uri): InputStream? =
        context.contentResolver.openInputStream(uri)

    fun resolveExtension(uri: Uri): String {
        if (ContentResolver.SCHEME_FILE == uri.scheme) {
            return MimeTypeMap.getFileExtensionFromUrl(uri.toString()) ?: "epub"
        }
        val mimeType = context.contentResolver.getType(uri)
        return if (mimeType != null) {
            resolveExtensionFromMimeType(mimeType = mimeType) ?: "epub"
        } else {
            "epub"
        }
    }

    fun resolveExtensionFromMimeType(mimeType: String): String? {
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }

    fun toUrl(file: File): AbsoluteUrl {
        return file.toUrl(isDirectory = false)
    }

    fun deleteFile(path: String): Boolean {
        return File(path).delete()
    }

    fun saveFileFromStream(input: InputStream, extension: String?): Try<File, Exception> {
        return try {
            val safeExtension = when {
                extension == null -> ".epub"
                extension.startsWith(prefix = ".") -> extension
                else -> ".$extension"
            }
            val filename = "${Uuid.random()}$safeExtension"
            val file = File(filesDir, filename)

            FileOutputStream(file).use { output ->
                input.copyTo(out = output)
            }
            Try.success(success = file)
        } catch (e: Exception) {
            Try.failure(failure = e)
        }
    }
}
