package com.triangles.domain.core.error

/**
 * A transport-agnostic failure that can safely cross the domain boundary.
 * It intentionally carries no Retrofit, Gson or other data-layer types.
 */
sealed interface AppError {

    data object NoInternet : AppError

    data object NoConnection : AppError

    data object Timeout : AppError

    data object Unauthorized : AppError

    data object NotFound : AppError

    data class RateLimited(val retryAfterSeconds: Long? = null) : AppError

    data object Server : AppError

    data class Api(val code: Int, val message: String?) : AppError

    data object Security : AppError

    data class Unknown(val cause: Throwable? = null) : AppError
}
