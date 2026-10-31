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

    @OptIn(ExperimentalPagingApi::class)
    override fun getMoves(): Flow<PagingData<Movie>> =
        Pager(
            config = PagingConfig(
                pageSize = 20,
                prefetchDistance = 5,
                enablePlaceholders = false
            ),
            remoteMediator = remoteMediator,
            pagingSourceFactory = localDataSource::getMovies
        ).flow.map { pagingData ->
                pagingData.map { entity -> domainMapper.map(entity) }
        }

    override fun getMovieById(movieId: Int): Flow<MovieDetails?> =
        localDataSource.getMovieById(movieId)
            .map { entity -> entity?.let(detailsMapper::map) }
}
