package com.triangles.data.network

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.triangles.data.network.response.BaseResponse
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.runBlocking
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class APIExecutionHandlerTest {

    private val handler = APIExecutionHandler(Gson())

    @Test
    fun `401 maps to Unauthorized`() = runBlocking {
        val result = handler.execute { errorResponse(401, """{"message":"expired"}""") }

        val failure = result.exceptionOrNull()
        assertTrue(failure is NetworkException.Unauthorized)
        assertEquals(401, (failure as NetworkException.Unauthorized).code)
    }

    @Test
    fun `404 maps to NotFound`() = runBlocking {
        val result = handler.execute { errorResponse(404, """{"message":"missing"}""") }

        assertTrue(result.exceptionOrNull() is NetworkException.NotFound)
    }

    @Test
    fun `429 maps to RateLimited and keeps Retry-After`() = runBlocking {
        val result = handler.execute {
            errorResponse(429, """{"message":"slow down"}""", "Retry-After" to "120")
        }

        val failure = result.exceptionOrNull()
        assertTrue(failure is NetworkException.RateLimited)
        assertEquals("120", (failure as NetworkException.RateLimited).retryAfter)
    }

    @Test
    fun `500 maps to Server`() = runBlocking {
        val result = handler.execute { errorResponse(500, """{"message":"boom"}""") }

        assertTrue(result.exceptionOrNull() is NetworkException.Server)
    }

    @Test
    fun `malformed error body does not break status mapping`() = runBlocking {
        val result = handler.execute { errorResponse(500, "{not json") }

        assertTrue(result.exceptionOrNull() is NetworkException.Server)
    }

    @Test
    fun `malformed response maps to DataParsing`() = runBlocking {
        val result = handler.execute<Any> { throw JsonSyntaxException("malformed") }

        assertTrue(result.exceptionOrNull() is NetworkException.DataParsing)
    }

    @Test
    fun `UnknownHostException maps to NoInternet`() = runBlocking {
        val result = handler.execute<Any> { throw UnknownHostException("offline") }

        assertTrue(result.exceptionOrNull() is NetworkException.NoInternet)
    }

    @Test
    fun `SocketTimeoutException maps to Timeout`() = runBlocking {
        val result = handler.execute<Any> { throw SocketTimeoutException("timeout") }

        assertTrue(result.exceptionOrNull() is NetworkException.Timeout)
    }

    @Test
    fun `CancellationException is rethrown`() {
        assertThrows(CancellationException::class.java) {
            runBlocking {
                handler.execute<Any> { throw CancellationException("cancelled") }
            }
        }
    }

    private fun errorResponse(
        code: Int,
        payload: String,
        vararg headers: Pair<String, String>
    ): Response<BaseResponse<Any>?> {
        val body = payload.toResponseBody("application/json".toMediaType())
        val headerBuilder = Headers.Builder()
        headers.forEach { (name, value) -> headerBuilder.add(name, value) }

        val rawResponse = okhttp3.Response.Builder()
            .request(Request.Builder().url("https://example.com/").build())
            .protocol(Protocol.HTTP_1_1)
            .code(code)
            .message("error")
            .headers(headerBuilder.build())
            .body(body)
            .build()

        return Response.error(body, rawResponse)
    }
}
