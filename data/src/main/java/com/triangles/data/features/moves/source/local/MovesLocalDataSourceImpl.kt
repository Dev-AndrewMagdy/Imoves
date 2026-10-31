package com.triangles.data.features.moves.source.local

import com.triangles.data.features.moves.source.local.dao.MovieDao
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class MovesLocalDataSourceImpl @Inject constructor(
    private val movieDao: MovieDao
) : MovesLocalDataSource {

    override fun observeMovies(): Flow<List<MovieEntity>> = movieDao.observeMovies()

    override suspend fun getMovies(): List<MovieEntity> = movieDao.getMovies()

    override suspend fun saveMovies(movies: List<MovieEntity>) = movieDao.upsertMovies(movies)

    override suspend fun clearMovies() = movieDao.clearMovies()
}
