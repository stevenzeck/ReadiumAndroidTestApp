package com.example.readiumandroidtestapp.core.domain.storage

import android.net.Uri
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import java.io.File
import java.io.InputStream

interface StorageGateway {
    val filesDir: File
    fun openInputStream(uri: Uri): InputStream?
    fun resolveExtension(uri: Uri): String
    fun resolveExtensionFromMimeType(mimeType: String): String?
    fun toUrl(file: File): AbsoluteUrl?
    fun deleteFile(path: String): Boolean
    fun saveFileFromStream(input: InputStream, extension: String?): Try<File, Exception>
}
