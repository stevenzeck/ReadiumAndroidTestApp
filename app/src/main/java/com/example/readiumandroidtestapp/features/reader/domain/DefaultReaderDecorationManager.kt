package com.example.readiumandroidtestapp.features.reader.domain

import com.example.readiumandroidtestapp.core.domain.model.Highlight
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import org.readium.navigator.common.Decoration
import org.readium.navigator.web.fixedlayout.FixedWebDecorationLocation
import org.readium.navigator.web.reflowable.ReflowableWebDecorationLocation
import org.readium.r2.shared.publication.Locator
import javax.inject.Inject
import org.readium.r2.navigator.Decoration as LegacyDecoration

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
    override fun pdfDecorationFlow(bookId: Long?): Flow<List<LegacyDecoration>> {
        if (bookId == null) return flowOf(value = emptyList())

        return bookRepository.highlightsForBook(bookId = bookId).map { highlights ->
            highlights.map { highlight ->
                val style = when (highlight.style) {
                    Highlight.Style.HIGHLIGHT -> LegacyDecoration.Style.Highlight(tint = highlight.tint)
                    Highlight.Style.UNDERLINE -> LegacyDecoration.Style.Underline(tint = highlight.tint)
                }
                LegacyDecoration(
                    id = highlight.id.toString(),
                    locator = highlight.locator,
                    style = style,
                )
            }
        }
    }

    override fun epubDecorationFlow(
        bookId: Long?,
        isFixedLayout: Boolean,
    ): Flow<List<Decoration<*>>> {
        if (bookId == null) return flowOf(value = emptyList())

        return bookRepository.highlightsForBook(bookId = bookId).map { highlights ->
            highlights.mapIndexedNotNull { _, highlight ->
                val style = when (highlight.style) {
                    Highlight.Style.HIGHLIGHT -> Decoration.Style.Highlight(
                        tint = highlight.tint,
                    )

                    Highlight.Style.UNDERLINE -> Decoration.Style.Underline(
                        tint = highlight.tint,
                    )
                }

                val location = if (isFixedLayout) {
                    FixedWebDecorationLocation(locator = highlight.locator)
                } else {
                    ReflowableWebDecorationLocation(locator = highlight.locator)
                } ?: return@mapIndexedNotNull null

                Decoration(
                    id = Decoration.Id(value = highlight.id.toString()),
                    location = location,
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
