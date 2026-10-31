package com.triangles.data.features.moves.source.remote

import com.triangles.data.features.moves.model.MovieResponse
import com.triangles.data.network.response.PaginationResponse

interface MovesRemoteDataSource {

    suspend fun getMovies(page: Int): Result<PaginationResponse<MovieResponse>>
}
