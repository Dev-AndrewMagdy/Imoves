package com.triangles.data.network

import com.triangles.domain.core.error.AppError
import com.triangles.domain.core.error.DomainErrorException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Single mapping point from any data-layer failure to a domain [AppError].
 * Keep this exhaustive so new [NetworkException] types cannot silently fall through.
 */
internal fun Throwable.toAppError(): AppError = when (this) {

    is DomainErrorException -> error

    is NetworkException.NoData -> AppError.Unknown(this)

    is NetworkException.Unauthorized -> AppError.Unauthorized

    is NetworkException.NotFound -> AppError.NotFound

    is NetworkException.RateLimited -> AppError.RateLimited(retryAfterSeconds())

    is NetworkException.Server -> AppError.Server

    is NetworkException.Api -> AppError.Api(code = code, message = message ?: errorCode)

    is NetworkException.NoInternet -> AppError.NoInternet

    is NetworkException.Connection -> AppError.NoConnection

    is NetworkException.Timeout -> AppError.Timeout

    is NetworkException.Security -> AppError.Security

    is NetworkException.DataParsing -> AppError.Unknown(this)

    is NetworkException.Unknown -> AppError.Unknown(this)

    else -> AppError.Unknown(this)
}

internal fun Throwable.toDomainException(): DomainErrorException =
    this as? DomainErrorException ?: DomainErrorException(toAppError())

/**
 * Parses the delta-seconds form of `Retry-After`. The HTTP-date form is treated as unknown.
 */
private fun NetworkException.RateLimited.retryAfterSeconds(): Long? =
    retryAfter?.trim()?.toLongOrNull()?.takeIf { it >= 0 }

internal fun Throwable.rethrowIfCancellation() {
    if (this is CancellationException) throw this
}
