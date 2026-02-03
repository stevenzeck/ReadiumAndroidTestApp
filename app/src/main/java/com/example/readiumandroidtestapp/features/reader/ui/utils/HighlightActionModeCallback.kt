package com.example.readiumandroidtestapp.features.reader.ui.utils

import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.readium.r2.navigator.SelectableNavigator
import org.readium.r2.shared.publication.Locator

/**
 * Custom ActionMode Callback that allows us to inject the navigator instance later.
 */
class HighlightActionModeCallback(
    private val onHighlight: (Locator) -> Unit,
) : ActionMode.Callback {

    var navigator: SelectableNavigator? = null

    override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
        menu.add(Menu.NONE, 100, Menu.NONE, "Highlight")
        return true
    }

    override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean = false

    override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
        if (item.itemId == 100) {
            val nav = navigator ?: return false

            val scope = (nav as? androidx.fragment.app.Fragment)?.viewLifecycleOwner?.lifecycleScope

            scope?.launch {
                val selection = nav.currentSelection()
                if (selection != null) {
                    onHighlight(selection.locator)
                    mode.finish()
                }
            }
            return true
        }
        return false
    }

    override fun onDestroyActionMode(mode: ActionMode) {}
}
