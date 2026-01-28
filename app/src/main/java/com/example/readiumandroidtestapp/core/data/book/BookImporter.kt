package com.example.readiumandroidtestapp.core.data.book

import android.net.Uri
import com.example.readiumandroidtestapp.core.domain.model.Book
import org.readium.r2.shared.util.AbsoluteUrl
import org.readium.r2.shared.util.Try

interface BookImporter {
    suspend fun importFromUrl(url: AbsoluteUrl): Try<Book, ImportError>
    suspend fun importFromUri(uri: Uri): Try<Book, ImportError>
}

sealed class ImportError {
    data object Network : ImportError()
    data object Storage : ImportError()
    data object InvalidBook : ImportError()
    data class Database(val cause: Exception) : ImportError()
    data class Unknown(val cause: Exception) : ImportError()
}
