package com.example.readiumandroidtestapp.features.appfunctions

import android.content.Intent
import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunction
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import com.example.readiumandroidtestapp.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Open a publication or audiobook by title.
 */
@RequiresApi(36)
@AndroidEntryPoint
@AppFunctionServiceEntryPoint(
    serviceName = "GeneratedLibraryService",
    appFunctionXmlFileName = "library_functions"
)
abstract class BaseLibraryAppFunctionService : AppFunctionService() {

    @Inject
    lateinit var bookRepository: BookRepository

    @AppFunction
    suspend fun openPublication(title: String) {
        // Simple fuzzy search: find first book where the title contains the search query
        val books = bookRepository.books.first()
        val book = books.firstOrNull { it.title?.contains(title, ignoreCase = true) == true }

        if (book != null) {
            val isAudiobook = book.rawMediaType.contains("audiobook", ignoreCase = true)

            // We are inside a Service, so `this` is a Context!
            val intent = Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("APP_FUNCTION_OPEN_BOOK_ID", book.id)
                putExtra("APP_FUNCTION_IS_AUDIOBOOK", isAudiobook)
            }
            startActivity(intent)
        }
    }
}
