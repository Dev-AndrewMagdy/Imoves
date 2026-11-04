package com.triangles.data.features.moves.source.remote

import com.triangles.data.features.moves.model.MovieResponse
import com.triangles.data.network.APIExecutionHandler
import com.triangles.data.network.response.BasePagingResponse
import com.triangles.data.network.rethrowIfCancellation
import com.triangles.data.network.toDomainException
import javax.inject.Inject

class MovesRemoteDataSourceImpl @Inject constructor(
    private val api: MovesApi,
    private val executionHandler: APIExecutionHandler
) : MovesRemoteDataSource {

    override suspend fun getMovies(page: Int): Result<BasePagingResponse<MovieResponse>> {
        val result = executionHandler.execute { api.getMoves(page) }
        val failure = result.exceptionOrNull() ?: return result

        failure.rethrowIfCancellation()

        return Result.failure(failure.toDomainException())
    }
}
