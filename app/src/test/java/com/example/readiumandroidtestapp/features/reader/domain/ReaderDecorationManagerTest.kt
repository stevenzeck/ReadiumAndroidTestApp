package com.example.readiumandroidtestapp.features.reader.domain

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.readiumandroidtestapp.core.domain.model.ReaderAnnotation
import com.example.readiumandroidtestapp.core.domain.repository.BookRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.navigator.Decoration
import org.readium.r2.shared.publication.Locator
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(manifest = Config.NONE)
class ReaderDecorationManagerTest {

    private val bookRepository: BookRepository = mockk()
    private val manager = ReaderDecorationManager(bookRepository = bookRepository)

    @Test
    fun `decorationFlow returns empty list when bookId is null`() = runTest {
        val result = manager.decorationFlow(bookId = null).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun `decorationFlow returns mapped decorations from repository`() = runTest {
        val bookId = 1L
        // Using primary constructor to avoid Locator complexity in test setup
        // and ensure id is set.
        val annotation = ReaderAnnotation(
            bookId = bookId,
            style = ReaderAnnotation.Style.HIGHLIGHT,
            tint = 123456,
            href = "http://example.com/chapter1",
            type = "text/html",
            title = "Chapter 1",
            annotation = "Test annotation",
        ).apply { id = 100L }

        coEvery { bookRepository.annotationsForBook(bookId = bookId) } returns flowOf(
            value = listOf(
                annotation,
            ),
        )

        val result = manager.decorationFlow(bookId = bookId).first()

        assertEquals(1, result.size)
        val decoration = result.first()
        assertEquals("100", decoration.id)
        // Check that the decoration locator matches the annotation's locator
        // Compare the objects produced by ReaderAnnotation.locator
        assertEquals(annotation.locator, decoration.locator)
        assertTrue(decoration.style is Decoration.Style.Highlight)
        assertEquals(123456, (decoration.style as Decoration.Style.Highlight).tint)
    }

    @Test
    fun `onAnnotateAction shows dialog`() = runTest {
        val locator = mockk<Locator>(relaxed = true)

        manager.onAnnotateAction(selection = locator)

        assertTrue(manager.showAnnotationDialog.value)
    }

    @Test
    fun `dismissAnnotationDialog hides dialog`() = runTest {
        val locator = mockk<Locator>(relaxed = true)
        manager.onAnnotateAction(selection = locator)
        assertTrue(manager.showAnnotationDialog.value)

        manager.dismissAnnotationDialog()

        assertFalse(manager.showAnnotationDialog.value)
    }

    @Test
    fun `saveAnnotation saves to repository and dismisses dialog`() = runTest {
        val bookId = 1L
        val note = "My Note"
        val color = 123456
        val locator = mockk<Locator>(relaxed = true)

        // Setup state
        manager.onAnnotateAction(selection = locator)

        coEvery {
            bookRepository.addAnnotation(
                bookId = bookId,
                style = ReaderAnnotation.Style.HIGHLIGHT,
                tint = color,
                locator = locator,
                annotation = note,
            )
        } returns 101L

        manager.saveAnnotation(
            bookId = bookId,
            note = note,
            color = color,
            style = ReaderAnnotation.Style.HIGHLIGHT,
        )

        coVerify {
            bookRepository.addAnnotation(
                bookId = bookId,
                style = ReaderAnnotation.Style.HIGHLIGHT,
                tint = color,
                locator = locator,
                annotation = note,
            )
        }

        assertFalse(manager.showAnnotationDialog.value)
    }
}
