package com.triangles.imoves.features.common

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Controls status/navigation bar icon contrast for the current screen.
 *
 * The activity is edge-to-edge, so [useDarkStatusBarIcons] must match the content drawn behind
 * the status bar (dark icons for light content, light icons for an image/scrim).
 */
@Composable
fun SystemBarIconsEffect(
    useDarkStatusBarIcons: Boolean,
    useDarkNavigationBarIcons: Boolean = true,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = useDarkStatusBarIcons
                isAppearanceLightNavigationBars = useDarkNavigationBarIcons
            }
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
