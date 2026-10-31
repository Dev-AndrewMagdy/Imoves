package com.triangles.data.features.moves.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.triangles.data.features.moves.mapper.MovieEntityMapper
import com.triangles.data.features.moves.source.local.MoviesLocalDataSource
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import com.triangles.data.features.moves.source.remote.MovesRemoteDataSource
import com.triangles.data.util.mapper.mapAll
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediator @Inject constructor(
    private val remoteDataSource: MovesRemoteDataSource,
    private val localDataSource: MoviesLocalDataSource,
    private val entityMapper: MovieEntityMapper
) : RemoteMediator<Int, MovieEntity>() {

    override suspend fun initialize(): InitializeAction =
        if (localDataSource.hasMovies()) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MovieEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1

            LoadType.PREPEND ->
                return MediatorResult.Success(endOfPaginationReached = true)

            LoadType.APPEND -> state.pages.size + 1
        }

        return try {
            val pagination = remoteDataSource.getMovies(page)
                .getOrElse { return MediatorResult.Error(it) }

            val movies = entityMapper.mapAll(pagination.results)
            val endOfPaginationReached = movies.isEmpty() ||
                (pagination.totalPages > 0 && pagination.page >= pagination.totalPages)

            localDataSource.saveMovies(
                movies = movies,
                clearFirst = loadType == LoadType.REFRESH
            )

            MediatorResult.Success(endOfPaginationReached)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            MediatorResult.Error(exception)
        }
    }
}
