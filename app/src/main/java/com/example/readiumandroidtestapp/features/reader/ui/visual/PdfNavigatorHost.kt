package com.example.readiumandroidtestapp.features.reader.ui.visual

import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.commitNow
import kotlinx.coroutines.launch
import org.readium.adapter.pdfium.navigator.PdfiumPreferences
import org.readium.adapter.pdfium.navigator.PdfiumPreferencesEditor
import org.readium.adapter.pdfium.navigator.PdfiumSettings
import org.readium.r2.navigator.VisualNavigator
import org.readium.r2.navigator.input.InputListener
import org.readium.r2.navigator.input.TapEvent
import org.readium.r2.navigator.pdf.PdfNavigatorFactory
import org.readium.r2.navigator.pdf.PdfNavigatorFragment
import org.readium.r2.shared.publication.Locator
import org.readium.r2.shared.publication.Publication

@Composable
fun PdfReader(
    publication: Publication,
    initialLocator: Locator?,
    initialPreferences: PdfiumPreferences?,
    onLocatorChanged: (Locator) -> Unit,
    pdfNavigatorFactory: PdfNavigatorFactory<PdfiumSettings, PdfiumPreferences, PdfiumPreferencesEditor>,
    onTap: () -> Unit,
    onNavigatorReady: (VisualNavigator) -> Unit,
) {
    val context = LocalContext.current
    val fragmentActivity = context as? FragmentActivity
    val containerId = rememberSaveable { mutableIntStateOf(value = View.generateViewId()) }

    val currentOnLocatorChanged by rememberUpdatedState(newValue = onLocatorChanged)
    val currentOnTap by rememberUpdatedState(newValue = onTap)
    val currentOnNavigatorReady by rememberUpdatedState(newValue = onNavigatorReady)

    DisposableEffect(key1 = publication) {
        if (fragmentActivity == null) return@DisposableEffect onDispose { }

        val fragmentManager = fragmentActivity.supportFragmentManager

        val factory = pdfNavigatorFactory.createFragmentFactory(
            initialLocator = initialLocator,
            initialPreferences = initialPreferences,
        )
        fragmentManager.fragmentFactory = factory

        var fragment = fragmentManager.findFragmentById(containerId.intValue) as? VisualNavigator

        if (fragment == null) {
            fragmentManager.commitNow(allowStateLoss = true) {
                replace(
                    containerId.intValue,
                    PdfNavigatorFragment::class.java,
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
        }

        val scope = kotlinx.coroutines.CoroutineScope(context = kotlinx.coroutines.Dispatchers.Main)
        val job = scope.launch(start = kotlinx.coroutines.CoroutineStart.DEFAULT) {
            fragment?.currentLocator?.collect { locator ->
                currentOnLocatorChanged(locator)
            }
        }

        onDispose {
            job.cancel()
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
        modifier = Modifier
            .fillMaxSize()
            .testTag(tag = "PdfNavigatorHost"),
        factory = { ctx ->
            FragmentContainerView(ctx).apply {
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
