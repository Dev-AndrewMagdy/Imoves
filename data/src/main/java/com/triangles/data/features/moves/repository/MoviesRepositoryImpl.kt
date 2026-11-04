package com.triangles.data.features.moves.repository

import androidx.paging.Pager
import androidx.paging.ExperimentalPagingApi
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.triangles.data.features.moves.mapper.MovieDetailsMapper
import com.triangles.data.features.moves.mapper.MovieDomainMapper
import com.triangles.data.features.moves.mediator.MovieRemoteMediator
import com.triangles.data.features.moves.source.local.MoviesLocalDataSource
import com.triangles.domain.features.moves.model.Movie
import com.triangles.domain.features.moves.model.MovieDetails
import com.triangles.domain.features.moves.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoviesRepositoryImpl @Inject constructor(
    private val localDataSource: MoviesLocalDataSource,
    private val domainMapper: MovieDomainMapper,
    private val detailsMapper: MovieDetailsMapper,
    private val remoteMediator: MovieRemoteMediator
) : MoviesRepository {

    /**
     * The backend has no search endpoint, so a query is matched against the local Room cache.
     * Searching therefore uses a local-only pager (no remote mediator) and never hits the network.
     */
    @OptIn(ExperimentalPagingApi::class)
    override fun getMoves(query: String): Flow<PagingData<Movie>> {
        val pager = if (query.isBlank()) {
            Pager(
                config = PAGING_CONFIG,
                remoteMediator = remoteMediator,
                pagingSourceFactory = { localDataSource.getMovies(query = "") }
            )
        } else {
            Pager(
                config = PAGING_CONFIG,
                pagingSourceFactory = { localDataSource.getMovies(query = query) }
            )
        }

        return pager.flow.map { pagingData ->
            pagingData.map { entity -> domainMapper.map(entity) }
        }
    }

    override fun getMovieById(movieId: Int): Flow<MovieDetails?> =
        localDataSource.getMovieById(movieId)
            .map { entity -> entity?.let(detailsMapper::map) }

    private companion object {
        val PAGING_CONFIG = PagingConfig(
            pageSize = 20,
            prefetchDistance = 5,
            enablePlaceholders = false
        )
    }
}
