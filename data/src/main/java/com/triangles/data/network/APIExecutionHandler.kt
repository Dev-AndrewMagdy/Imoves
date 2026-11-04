package com.triangles.data.network

import com.google.gson.Gson
import com.google.gson.JsonParseException
import com.google.gson.stream.MalformedJsonException
import com.triangles.data.network.response.BaseResponse
import com.triangles.data.network.response.ErrorResponse
import com.triangles.data.util.TAG
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.net.ConnectException
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton
import javax.net.ssl.SSLException
import kotlin.coroutines.cancellation.CancellationException

@Singleton
class APIExecutionHandler @Inject constructor(private val gson: Gson) {

    suspend fun <T : Any> execute(
        call: suspend () -> Response<BaseResponse<T>?>
    ): Result<T> = safeCall {
        val response = call()

        if (response.isSuccessful) {
            response.extractData()
        } else {
            Result.failure(response.toHttpException())
        }
    }

    private suspend inline fun <R> safeCall(
        crossinline block: suspend () -> Result<R>
    ): Result<R> {
        return try {
            block().onFailure {
                Timber.tag(this@APIExecutionHandler.TAG).e(it, "API request failed")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            val exception = e.toNetworkException()

            Timber.tag(this@APIExecutionHandler.TAG).e(exception, "API execution failed")

            Result.failure(exception)
        }
    }

    private fun <T : Any> Response<BaseResponse<T>?>.extractData(): Result<T> {
        val data = body()?.data

        return if (data == null) {
            Result.failure(NetworkException.NoData())
        } else {
            Result.success(data)
        }
    }

    private fun Response<*>.toHttpException(): NetworkException {
        val error = parseError()
        val message = error?.errorType ?: error?.message

        return when (val status = code()) {

            HTTP_UNAUTHORIZED, HTTP_FORBIDDEN ->
                NetworkException.Unauthorized(
                    code = status,
                    error = message
                )

            HTTP_NOT_FOUND ->
                NetworkException.NotFound(message)

            429 ->
                NetworkException.RateLimited(
                    retryAfter = headers()["Retry-After"],
                    error = message
                )

            in 500..599 ->
                NetworkException.Server(
                    message = message
                )

            else ->
                NetworkException.Api(
                    code = status,
                    errorCode = error?.errorType,
                    message = message
                )
        }
    }

    private fun Response<*>.parseError(): ErrorResponse? {
        return runCatching {
            errorBody()?.charStream()?.use {
                gson.fromJson(it, ErrorResponse::class.java)
            }
        }.getOrNull()
    }

    private fun Throwable.toNetworkException(): NetworkException =
        when (this) {

            is UnknownHostException ->
                NetworkException.NoInternet(this)

            is ConnectException ->
                NetworkException.Connection(this)

            is SocketTimeoutException,
            is TimeoutException ->
                NetworkException.Timeout(
                    message = message,
                    cause = this
                )

            is SSLException ->
                NetworkException.Security(
                    message = message,
                    cause = this
                )

            is JsonParseException, is MalformedJsonException ->
                NetworkException.DataParsing(this)

            is IOException ->
                NetworkException.Connection(this)

            else ->
                NetworkException.Unknown(this)
        }
}