package com.triangles.data.network

import com.google.gson.JsonSyntaxException
import com.triangles.domain.core.error.AppError
import com.triangles.domain.core.error.DomainErrorException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class ToAppErrorTest {

    @Test
    fun `maps every NetworkException subclass`() {
        val io = IOException("io")

        val cases = listOf(
            NetworkException.Unauthorized(401, "unauthorized") to AppError.Unauthorized,
            NetworkException.NotFound("missing") to AppError.NotFound,
            NetworkException.RateLimited("120", "slow down") to AppError.RateLimited(120),
            NetworkException.RateLimited("Wed, 21 Oct 2015 07:28:00 GMT", null) to AppError.RateLimited(null),
            NetworkException.Server("boom") to AppError.Server,
            NetworkException.Api(422, "VALIDATION", "invalid") to AppError.Api(422, "invalid"),
            NetworkException.Api(422, "VALIDATION", null) to AppError.Api(422, "VALIDATION"),
            NetworkException.NoInternet(UnknownHostException()) to AppError.NoInternet,
            NetworkException.Connection(ConnectException()) to AppError.NoConnection,
            NetworkException.Timeout("timeout", SocketTimeoutException()) to AppError.Timeout,
            NetworkException.Security("ssl", SSLException("ssl")) to AppError.Security
        )

        cases.forEach { (throwable, expected) ->
            assertEquals(expected, throwable.toAppError())
        }
    }

    @Test
    fun `subclasses without a dedicated AppError map to Unknown`() {
        assertTrue(NetworkException.NoData().toAppError() is AppError.Unknown)
        assertTrue(NetworkException.DataParsing(JsonSyntaxException("bad")).toAppError() is AppError.Unknown)
        assertTrue(NetworkException.Unknown(IOException("io")).toAppError() is AppError.Unknown)
    }

    @Test
    fun `DomainErrorException passes its AppError through`() {
        val original = DomainErrorException(AppError.Timeout)

        assertSame(original.error, original.toAppError())
    }

    @Test
    fun `unexpected throwable maps to Unknown and keeps its cause`() {
        val cause = IllegalStateException("unexpected")

        val mapped = cause.toAppError()

        assertTrue(mapped is AppError.Unknown)
        assertSame(cause, (mapped as AppError.Unknown).cause)
    }

    @Test
    fun `toDomainException does not double wrap`() {
        val original = DomainErrorException(AppError.Server)

        assertSame(original, original.toDomainException())
    }

    @Test
    fun `toDomainException converts data failures once`() {
        val unauthorized = NetworkException.Unauthorized(401, null).toDomainException()
        val unknown = IllegalStateException("unexpected").toDomainException()

        assertEquals(AppError.Unauthorized, unauthorized.error)
        assertTrue(unknown.error is AppError.Unknown)
    }
}
