package com.example.readiumandroidtestapp.features.reader.ui.visual

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commitNow
import com.example.readiumandroidtestapp.features.reader.ui.utils.HighlightActionModeCallback
import kotlinx.coroutines.launch
import org.readium.r2.navigator.SelectableNavigator
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.epub.EpubNavigatorFactory
import org.readium.r2.navigator.epub.EpubNavigatorFragment
import org.readium.r2.navigator.epub.EpubPreferences
import org.readium.r2.navigator.input.InputListener
import org.readium.r2.navigator.input.TapEvent
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

@Composable
fun EpubReader(
    publication: Publication,
    initialLocator: Locator?,
    initialPreferences: EpubPreferences,
    onLocatorChanged: (Locator) -> Unit,
    onTap: () -> Unit,
    onNavigatorReady: (VisualNavigator) -> Unit,
    onHighlight: (Locator) -> Unit,
) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    val containerId = rememberSaveable { mutableIntStateOf(value = View.generateViewId()) }

    val currentOnLocatorChanged by rememberUpdatedState(newValue = onLocatorChanged)
    val currentOnTap by rememberUpdatedState(newValue = onTap)
    val currentOnNavigatorReady by rememberUpdatedState(newValue = onNavigatorReady)
    val currentOnHighlight by rememberUpdatedState(newValue = onHighlight)

    val actionModeCallback = remember {
        HighlightActionModeCallback(onHighlight = { loc -> currentOnHighlight(loc) })
    }

    DisposableEffect(key1 = publication) {
        if (fragmentActivity == null) return@DisposableEffect onDispose { }

        val fragmentManager = fragmentActivity.supportFragmentManager

        val config = EpubNavigatorFragment.Configuration().apply {
            selectionActionModeCallback = actionModeCallback
        }

        val factory = EpubNavigatorFactory(publication = publication).createFragmentFactory(
            initialLocator = initialLocator,
            initialPreferences = initialPreferences,
            configuration = config,
        )

        fragmentManager.fragmentFactory = factory

        var fragment = fragmentManager.findFragmentById(containerId.intValue) as? VisualNavigator

        if (fragment == null) {
            fragmentManager.commitNow(allowStateLoss = true) {
                replace(
                    containerId.intValue,
                    EpubNavigatorFragment::class.java,
                    null,
                    null,
                )
            }
            fragment = fragmentManager.findFragmentById(containerId.intValue) as? VisualNavigator
        }

        val inputListener = object : InputListener {
            override fun onTap(event: TapEvent): Boolean {
                currentOnTap()
                return true
            }
        }
        fragment?.addInputListener(inputListener)

        if (fragment != null) {
            currentOnNavigatorReady(fragment)

            if (fragment is SelectableNavigator) {
                actionModeCallback.navigator = fragment
            }
        }

        val scope = kotlinx.coroutines.CoroutineScope(context = kotlinx.coroutines.Dispatchers.Main)
        val job = scope.launch(start = kotlinx.coroutines.CoroutineStart.DEFAULT) {
            fragment?.currentLocator?.collect { locator ->
                currentOnLocatorChanged(locator)
            }
        }

        onDispose {
            job.cancel()
            actionModeCallback.navigator = null
            fragment?.removeInputListener(inputListener)
            if (!fragmentManager.isStateSaved) {
                fragmentManager.commitNow(allowStateLoss = true) {
                    val currentFrag = fragmentManager.findFragmentById(containerId.intValue)
                    if (currentFrag != null) {
                        remove(currentFrag)
                    }
                }
            }
        }
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            FragmentContainerView(context = ctx).apply {
                id = containerId.intValue
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
        },
        update = { },
    )
}
