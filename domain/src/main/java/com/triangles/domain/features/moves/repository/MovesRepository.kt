package com.triangles.domain.features.moves.repository

import com.triangles.domain.features.moves.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovesRepository {

    /**
     * Emits the cached movies, ordered by popularity.
     */
    fun observeMovies(): Flow<List<Movie>>

    /**
     * Fetches the given [page] from the network, caches it and returns the fetched movies.
     */
    suspend fun refreshMovies(page: Int = 1): Result<List<Movie>>
}
