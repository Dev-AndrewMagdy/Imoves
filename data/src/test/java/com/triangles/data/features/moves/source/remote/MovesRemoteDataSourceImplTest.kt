package com.triangles.data.features.moves.source.remote

import com.google.gson.Gson
import com.triangles.data.features.moves.model.MovieResponse
import com.triangles.data.network.APIExecutionHandler
import com.triangles.data.network.response.BasePagingResponse
import com.triangles.data.network.response.BaseResponse
import com.triangles.domain.core.error.AppError
import com.triangles.domain.core.error.DomainErrorException
import java.net.UnknownHostException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class MovesRemoteDataSourceImplTest {

    @Test
    fun `failures leave the remote source as DomainErrorException`() = runBlocking {
        val dataSource = MovesRemoteDataSourceImpl(
            api = FailingApi(),
            executionHandler = APIExecutionHandler(Gson())
        )

        val result = dataSource.getMovies(page = 1)

        val failure = result.exceptionOrNull()
        assertTrue(failure is DomainErrorException)
        assertEquals(AppError.NoInternet, (failure as DomainErrorException).error)
    }

    @Test
    fun `cancellation is not wrapped`() {
        val dataSource = MovesRemoteDataSourceImpl(
            api = CancellingApi(),
            executionHandler = APIExecutionHandler(Gson())
        )

        assertThrows(CancellationException::class.java) {
            runBlocking { dataSource.getMovies(page = 1) }
        }
    }

    private class FailingApi : MovesApi {
        override suspend fun getMoves(
            page: Int
        ): Response<BaseResponse<BasePagingResponse<MovieResponse>>?> =
            throw UnknownHostException("offline")
    }

    private class CancellingApi : MovesApi {
        override suspend fun getMoves(
            page: Int
        ): Response<BaseResponse<BasePagingResponse<MovieResponse>>?> =
            throw CancellationException("cancelled")
    }
}
