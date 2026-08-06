package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.readium.r2.navigator.Decoration
import org.readium.r2.shared.publication.Locator
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReaderDecorationManager @Inject constructor(
    private val bookRepository: BookRepository,
) {
    // UI State for the Annotation Dialog
    val showAnnotationDialog: StateFlow<Boolean> field = MutableStateFlow(value = false)
    val editingAnnotation: StateFlow<ReaderAnnotation?> field = MutableStateFlow<ReaderAnnotation?>(
        value = null,
    )

    private var activeSelection: Locator? = null

    /**
     * Returns a Flow of Decoration objects (highlights/underlines) for the specific book ID.
     * This flow automatically updates when the database changes.
     */
    fun decorationFlow(bookId: Long?): Flow<List<Decoration>> {
        if (bookId == null) return flowOf(value = emptyList())

        return bookRepository.annotationsForBook(bookId).map { annotations ->
            annotations.map { annotation ->
                val style = when (annotation.style) {
                    ReaderAnnotation.Style.HIGHLIGHT -> Decoration.Style.Highlight(tint = annotation.tint)
                    ReaderAnnotation.Style.UNDERLINE -> Decoration.Style.Underline(tint = annotation.tint)
                }
                Decoration(
                    id = annotation.id.toString(),
                    locator = annotation.locator,
                    style = style,
                )
            }
        }
    }

    /**
     * Called when the user selects text and taps "Annotate" in the context menu.
     */
    fun onAnnotateAction(selection: Locator) {
        activeSelection = selection
        showAnnotationDialog.value = true
    }

    /**
     * Called when the user wants to edit an existing annotation from the TOC list.
     */
    fun onEditAnnotationAction(annotation: ReaderAnnotation) {
        editingAnnotation.value = annotation
        showAnnotationDialog.value = true
    }

    fun dismissAnnotationDialog() {
        showAnnotationDialog.value = false
        activeSelection = null
        editingAnnotation.value = null
    }

    /**
     * Persists the annotation to the database.
     */
    suspend fun saveAnnotation(
        bookId: Long,
        note: String,
        color: Int,
        style: ReaderAnnotation.Style,
    ) {
        val editing = editingAnnotation.value
        if (editing != null) {
            bookRepository.updateAnnotationNote(id = editing.id, note = note)
            bookRepository.updateAnnotationStyle(id = editing.id, style = style, tint = color)
        } else {
            val locator = activeSelection ?: return
            bookRepository.addAnnotation(
                bookId = bookId,
                style = style,
                tint = color,
                locator = locator,
                annotation = note,
            )
        }
        dismissAnnotationDialog()
    }
}
