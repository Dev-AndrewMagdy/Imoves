package com.triangles.data.features.moves.source.remote

import com.triangles.data.features.moves.model.MovieResponse
import com.triangles.data.network.APIExecutionHandler
import com.triangles.data.network.response.BasePagingResponse
import javax.inject.Inject

class MovesRemoteDataSourceImpl @Inject constructor(
    private val api: MovesApi,
    private val executionHandler: APIExecutionHandler
) : MovesRemoteDataSource {

    override suspend fun getMovies(page: Int): Result<BasePagingResponse<MovieResponse>> =
        executionHandler.execute { api.getMoves(page) }
}
