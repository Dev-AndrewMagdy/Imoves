package com.triangles.imoves.base.state

import androidx.annotation.StringRes
import androidx.compose.material3.SnackbarDuration

sealed interface UiOverlayEvent {

    data class SnackBar(
        val message: UiText,
        val actionLabel: String? = null,
        val duration: SnackbarDuration = SnackbarDuration.Short,
    ) : UiOverlayEvent

    data class Toast(
        val message: UiText,
        val duration: ToastDuration = ToastDuration.Short,
    ) : UiOverlayEvent

    data class Dialog(
        val title: UiText,
        val message: UiText,
        @param:StringRes val positiveText: Int = android.R.string.ok,
        @param:StringRes val negativeText: Int? = android.R.string.cancel,
        val cancelable: Boolean = true,
    ) : UiOverlayEvent

    data object DismissDialog : UiOverlayEvent
}

enum class ToastDuration { Short, Long }
