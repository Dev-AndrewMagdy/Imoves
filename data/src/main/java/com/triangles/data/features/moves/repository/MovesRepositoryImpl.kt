package com.triangles.data.features.moves.repository

import com.triangles.data.features.moves.mapper.MovieDomainMapper
import com.triangles.data.features.moves.mapper.MovieEntityMapper
import com.triangles.data.features.moves.source.local.MovesLocalDataSource
import com.triangles.data.features.moves.source.remote.MovesRemoteDataSource
import com.triangles.data.util.mapper.mapAll
import com.triangles.domain.features.moves.model.Movie
import com.triangles.domain.features.moves.repository.MovesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovesRepositoryImpl @Inject constructor(
    private val remoteDataSource: MovesRemoteDataSource,
    private val localDataSource: MovesLocalDataSource,
    private val responseToEntityMapper: MovieEntityMapper,
    private val entityToDomainMapper: MovieDomainMapper
) : MovesRepository {

    override fun observeMovies(): Flow<List<Movie>> =
        localDataSource.observeMovies()
            .map(entityToDomainMapper::mapAll)

    override suspend fun refreshMovies(page: Int): Result<List<Movie>> =
        remoteDataSource.getMovies(page)
            .map { pagination ->
                val entities = responseToEntityMapper.mapAll(pagination.results)
                localDataSource.saveMovies(entities)
                entityToDomainMapper.mapAll(entities)
            }
}
