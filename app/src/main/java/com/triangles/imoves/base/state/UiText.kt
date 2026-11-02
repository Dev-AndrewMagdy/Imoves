package com.triangles.imoves.base.state

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

sealed interface UiText {
    data class Plain(val value: String) : UiText
    data class Res(@param:StringRes val id: Int, val args: List<Any> = emptyList()) : UiText
}

fun UiText.asString(context: Context): String = when (this) {
    is UiText.Plain -> value
    is UiText.Res -> context.getString(id, *args.toTypedArray())
}

@Composable
fun UiText.asString(): String = asString(LocalContext.current)
