package com.triangles.data.features.moves.source.local

import androidx.paging.PagingSource
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import com.triangles.data.features.moves.source.local.entity.RemotePageState
import kotlinx.coroutines.flow.Flow

interface MoviesLocalDataSource {

    fun getMovies(query: String): PagingSource<Int, MovieEntity>

    fun getMovieById(movieId: Int): Flow<MovieEntity?>

    suspend fun hasMovies(): Boolean

    suspend fun getRemotePageState(): RemotePageState?

    suspend fun saveMovies(
        movies: List<MovieEntity>,
        pageState: RemotePageState?,
        clearFirst: Boolean = false
    )

    suspend fun clearMovies()
}
