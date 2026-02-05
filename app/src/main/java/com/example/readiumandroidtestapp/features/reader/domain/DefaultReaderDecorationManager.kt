package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.domain.model.Highlight
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.readium.r2.navigator.Decoration
import org.readium.r2.shared.publication.Locator
import javax.inject.Inject

class DefaultReaderDecorationManager @Inject constructor(
    private val bookRepository: BookRepository,
) : ReaderDecorationManager {
    // UI State for the Highlight Dialog
    private val _showHighlightDialog = MutableStateFlow(value = false)
    override val showHighlightDialog: StateFlow<Boolean> = _showHighlightDialog.asStateFlow()

    private var activeSelection: Locator? = null

    /**
     * Returns a Flow of Decoration objects (highlights/underlines) for the specific book ID.
     * This flow automatically updates when the database changes.
     */
    override fun decorationFlow(bookId: Long?): Flow<List<Decoration>> {
        if (bookId == null) return flowOf(value = emptyList())

        return bookRepository.highlightsForBook(bookId).map { highlights ->
            highlights.map { highlight ->
                val style = when (highlight.style) {
                    Highlight.Style.HIGHLIGHT -> Decoration.Style.Highlight(tint = highlight.tint)
                    Highlight.Style.UNDERLINE -> Decoration.Style.Underline(tint = highlight.tint)
                }
                Decoration(
                    id = highlight.id.toString(),
                    locator = highlight.locator,
                    style = style,
                )
            }
        }
    }

    /**
     * Called when the user selects text and taps "Highlight" in the context menu.
     */
    override fun onHighlightAction(selection: Locator) {
        activeSelection = selection
        _showHighlightDialog.value = true
    }

    override fun dismissHighlightDialog() {
        _showHighlightDialog.value = false
        activeSelection = null
    }

    /**
     * Persists the highlight to the database.
     */
    override suspend fun saveHighlight(bookId: Long, note: String, color: Int) {
        val locator = activeSelection ?: return
        bookRepository.addHighlight(
            bookId = bookId,
            style = Highlight.Style.HIGHLIGHT,
            tint = color,
            locator = locator,
            annotation = note,
        )
        dismissHighlightDialog()
    }
}
