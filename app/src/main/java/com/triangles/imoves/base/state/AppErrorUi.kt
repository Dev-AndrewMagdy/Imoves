package com.triangles.imoves.base.state

import androidx.annotation.StringRes
import com.triangles.domain.core.error.AppError
import com.triangles.domain.core.error.DomainErrorException
import com.triangles.imoves.R

fun Throwable.asAppError(): AppError =
    (this as? DomainErrorException)?.error ?: AppError.Unknown(this)

val AppError.isRetryable: Boolean
    get() = this !is AppError.Unauthorized

@StringRes
fun AppError.toMessageRes(): Int = when (this) {
    AppError.NoInternet -> R.string.error_no_internet
    AppError.NoConnection -> R.string.error_no_connection
    AppError.Timeout -> R.string.error_timeout
    AppError.Unauthorized -> R.string.error_unauthorized
    AppError.NotFound -> R.string.error_not_found
    is AppError.RateLimited -> R.string.error_rate_limited
    AppError.Server -> R.string.error_server
    is AppError.Api -> R.string.error_generic
    AppError.Security -> R.string.error_no_connection
    is AppError.Unknown -> R.string.error_generic
}


fun AppError.toUiText(): UiText = UiText.Res(toMessageRes())