package com.triangles.data.features.moves.source.local

import androidx.paging.PagingSource
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

interface MoviesLocalDataSource {

    fun getMovies(): PagingSource<Int, MovieEntity>

    fun getMovieById(movieId: Int): Flow<MovieEntity?>

    suspend fun hasMovies(): Boolean

    suspend fun saveMovies(movies: List<MovieEntity>, clearFirst: Boolean = false)

    suspend fun clearMovies()
}
