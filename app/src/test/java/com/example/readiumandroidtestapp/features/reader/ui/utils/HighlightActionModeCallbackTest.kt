package com.example.readiumandroidtestapp.features.reader.ui.utils

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.readium.r2.navigator.SelectableNavigator
import org.readium.r2.navigator.Selection
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.util.Url
import org.readium.r2.shared.util.mediatype.MediaType
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class HighlightActionModeCallbackTest {

    private lateinit var onHighlight: (Locator) -> Unit
    private lateinit var callback: HighlightActionModeCallback

    private val actionMode: ActionMode = mockk(relaxed = true)
    private val menu: Menu = mockk(relaxed = true)
    private val menuItem: MenuItem = mockk(relaxed = true)

    @Before
    fun setUp() {
        onHighlight = spyk()
        callback = HighlightActionModeCallback(onHighlight)
    }

    @Test
    fun `onCreateActionMode adds highlight menu item and returns true`() {
        val result = callback.onCreateActionMode(actionMode, menu)

        verify { menu.add(Menu.NONE, 100, Menu.NONE, "Highlight") }
        assertTrue(result)
    }

    @Test
    fun `onPrepareActionMode returns false`() {
        val result = callback.onPrepareActionMode(actionMode, menu)
        assertFalse(result)
    }

    @Test
    fun `onActionItemClicked with incorrect item id returns false`() {
        every { menuItem.itemId } returns 99

        val result = callback.onActionItemClicked(actionMode, menuItem)

        assertFalse(result)
        verify { onHighlight wasNot Called }
        verify(exactly = 0) { actionMode.finish() }
    }

    @Test
    fun `onActionItemClicked with correct item id but null navigator returns false`() {
        every { menuItem.itemId } returns 100
        callback.navigator = null

        val result = callback.onActionItemClicked(actionMode, menuItem)

        assertFalse(result)
        verify { onHighlight wasNot Called }
    }

    @Test
    fun `onActionItemClicked with navigator that is not a Fragment does nothing`() {
        every { menuItem.itemId } returns 100
        val navigator = mockk<SelectableNavigator>()
        callback.navigator = navigator

        val result = callback.onActionItemClicked(actionMode, menuItem)

        assertTrue(result)
        coVerify(exactly = 0) { navigator.currentSelection() }
        verify { onHighlight wasNot Called }
        verify(exactly = 0) { actionMode.finish() }
    }

    @Test
    fun `onActionItemClicked with navigator but no selection does not call onHighlight or finish`() =
        runTest {
            every { menuItem.itemId } returns 100
            val fragmentNavigator = mockk<FragmentNavigator>(relaxed = true)
            val lifecycleOwner = mockk<LifecycleOwner>()
            val lifecycleRegistry = LifecycleRegistry(provider = lifecycleOwner)

            every { fragmentNavigator.viewLifecycleOwner } returns lifecycleOwner
            every { lifecycleOwner.lifecycle } returns lifecycleRegistry
            lifecycleRegistry.handleLifecycleEvent(event = Lifecycle.Event.ON_RESUME)

            callback.navigator = fragmentNavigator

            coEvery { fragmentNavigator.currentSelection() } returns null

            val result = callback.onActionItemClicked(actionMode, menuItem)
            advanceUntilIdle()

            assertTrue(result)
            verify { onHighlight wasNot Called }
            verify(exactly = 0) { actionMode.finish() }
        }

    @Test
    fun `onActionItemClicked with selection calls onHighlight and finishes`() = runTest {
        every { menuItem.itemId } returns 100
        val fragmentNavigator = mockk<FragmentNavigator>(relaxed = true)
        val lifecycleOwner = mockk<LifecycleOwner>()
        val lifecycleRegistry = LifecycleRegistry(provider = lifecycleOwner)

        every { fragmentNavigator.viewLifecycleOwner } returns lifecycleOwner
        every { lifecycleOwner.lifecycle } returns lifecycleRegistry
        lifecycleRegistry.handleLifecycleEvent(event = Lifecycle.Event.ON_RESUME)

        callback.navigator = fragmentNavigator

        val locator = Locator(
            href = Url(url = "test.html")!!,
            mediaType = MediaType(string = "text/html")!!,
        )
        val selection = Selection(locator = locator, rect = null)
        coEvery { fragmentNavigator.currentSelection() } returns selection

        val result = callback.onActionItemClicked(mode = actionMode, item = menuItem)
        advanceUntilIdle()

        assertTrue(result)
        verify { onHighlight(locator) }
        verify { actionMode.finish() }
    }

    @Test
    fun `onDestroyActionMode is callable`() {
        callback.onDestroyActionMode(actionMode)
    }

    private abstract class FragmentNavigator : Fragment(), SelectableNavigator
}
