package com.example.readiumandroidtestapp.core.data.storage

import android.net.Uri
import com.example.readiumandroidtestapp.core.domain.storage.StorageGateway
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try
import org.readium.r2.shared.util.toUrl
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class FakeStorageGateway(
    override val filesDir: File,
) : StorageGateway {

    override fun openInputStream(uri: Uri): InputStream {
        return ByteArrayInputStream("Fake Book Content".toByteArray())
    }

    override fun resolveExtension(uri: Uri) = "epub"

    override fun resolveExtensionFromMimeType(mimeType: String): String? {
        return if (mimeType == "application/epub+zip") "epub" else null
    }

    var urlToReturn: AbsoluteUrl? = null

    override fun toUrl(file: File): AbsoluteUrl {
        return urlToReturn ?: file.toUrl()
    }

    override fun deleteFile(path: String): Boolean {
        return File(path).delete()
    }

    override fun saveFileFromStream(input: InputStream, extension: String?): Try<File, Exception> {
        val name = "test_${System.nanoTime()}.${extension ?: "epub"}"
        val file = File(filesDir, name)
        file.outputStream().use { output -> input.copyTo(output) }
        return Try.success(success = file)
    }
}
