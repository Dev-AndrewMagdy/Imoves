package com.triangles.data.network

sealed class NetworkException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause) {

    class NoData : NetworkException()

    class Unauthorized(
        val code: Int,
        val error: String?,
    ) : NetworkException(error)

    class NotFound(
        val error: String?,
    ) : NetworkException(error)

    class RateLimited(
        val retryAfter: String?,
        val error: String?,
    ) : NetworkException(error)

    class Server(
        message: String?,
    ) : NetworkException(message)

    class Api(
        val code: Int,
        val errorCode: String?,
        message: String?,
    ) : NetworkException(message)

    class NoInternet(
        cause: Throwable,
    ) : NetworkException(cause = cause)

    class Connection(
        cause: Throwable,
    ) : NetworkException(cause = cause)

    class Timeout(
        message: String?,
        cause: Throwable,
    ) : NetworkException(message, cause)

    class Security(
        message: String?,
        cause: Throwable,
    ) : NetworkException(message, cause)

    class DataParsing(
        cause: Throwable,
    ) : NetworkException(cause = cause)

    class Unknown(
        cause: Throwable,
    ) : NetworkException(cause = cause)
}