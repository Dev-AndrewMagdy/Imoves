package com.triangles.data.features.moves.source.local

import com.triangles.data.features.moves.source.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

interface MovesLocalDataSource {

    fun observeMovies(): Flow<List<MovieEntity>>

    suspend fun getMovies(): List<MovieEntity>

    suspend fun hasMovies(): Boolean

    suspend fun saveMovies(movies: List<MovieEntity>, clearFirst: Boolean = false)

    suspend fun clearMovies()
}
