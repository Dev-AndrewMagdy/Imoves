package com.triangles.data.features.moves.source.local

import androidx.paging.PagingSource
import androidx.room.withTransaction
import com.triangles.data.database.MovesDatabase
import com.triangles.data.features.moves.source.local.dao.MovieDao
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import com.triangles.data.features.moves.source.local.entity.RemotePageState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovesLocalDataSourceImpl @Inject constructor(
    private val database: MovesDatabase,
    private val movieDao: MovieDao
) : MoviesLocalDataSource {

    override fun getMovies(query: String): PagingSource<Int, MovieEntity> =
        movieDao.getMovies(query)

    override fun getMovieById(movieId: Int): Flow<MovieEntity?> = movieDao.getMovieById(movieId)

    override suspend fun hasMovies(): Boolean = movieDao.hasMovies()

    override suspend fun getRemotePageState(): RemotePageState? =
        movieDao.getRemotePageState()

    override suspend fun saveMovies(
        movies: List<MovieEntity>,
        pageState: RemotePageState?,
        clearFirst: Boolean
    ) {
        database.withTransaction {
            if (clearFirst) {
                movieDao.clearMovies()
                movieDao.clearRemotePageState()
            }

            movieDao.upsertMovies(movies)
            pageState?.let { movieDao.upsertRemotePageState(it) }
        }
    }

    override suspend fun clearMovies() = movieDao.clearMovies()
}
