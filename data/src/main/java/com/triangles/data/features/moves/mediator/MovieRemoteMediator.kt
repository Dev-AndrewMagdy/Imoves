package com.triangles.data.features.moves.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.triangles.data.features.moves.mapper.MovieEntityMapper
import com.triangles.data.features.moves.source.local.MoviesLocalDataSource
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import com.triangles.data.features.moves.source.local.entity.RemotePageState
import com.triangles.data.features.moves.source.remote.MovesRemoteDataSource
import com.triangles.data.network.rethrowIfCancellation
import com.triangles.data.network.toDomainException
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
            LoadType.REFRESH -> FIRST_PAGE

            LoadType.PREPEND ->
                return MediatorResult.Success(endOfPaginationReached = true)

            // Do not derive the page from state.pages. Paging can pass an APPEND request whose
            // PagingState was captured before the local refresh populated Room, and relying on it
            // would report the end of pagination on the very first scroll. The persisted state is
            // the source of truth.
            LoadType.APPEND -> {
                val pageState = localDataSource.getRemotePageState()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)

                if (pageState.reachedEnd) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                pageState.nextPage
            }
        }

        // The remote source already reports DomainErrorException failures.
        val pagination = remoteDataSource.getMovies(page)
            .getOrElse { exception ->
                exception.rethrowIfCancellation()
                return MediatorResult.Error(exception)
            }

        val movies = entityMapper.mapAll(pagination.results)
        val endOfPaginationReached = movies.isEmpty() ||
            (pagination.totalPages > 0 && pagination.page >= pagination.totalPages)

        val nextPageState = RemotePageState(
            nextPage = if (endOfPaginationReached) page else page + 1,
            reachedEnd = endOfPaginationReached
        )

        return try {
            localDataSource.saveMovies(
                movies = movies,
                pageState = nextPageState,
                clearFirst = loadType == LoadType.REFRESH
            )

            MediatorResult.Success(endOfPaginationReached)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            MediatorResult.Error(exception.toDomainException())
        }
    }

    private companion object {
        const val FIRST_PAGE = 1
    }
}
