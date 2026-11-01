package com.triangles.data.features.moves.mediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator.MediatorResult
import com.triangles.data.features.moves.mapper.MovieEntityMapper
import com.triangles.data.features.moves.model.MovieResponse
import com.triangles.data.features.moves.source.local.MoviesLocalDataSource
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import com.triangles.data.features.moves.source.remote.MovesRemoteDataSource
import com.triangles.data.network.response.BasePagingResponse
import com.triangles.domain.core.error.AppError
import com.triangles.domain.core.error.DomainErrorException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediatorTest {

    private val pagingState = PagingState<Int, MovieEntity>(
        pages = emptyList(),
        anchorPosition = null,
        config = PagingConfig(pageSize = 20),
        leadingPlaceholderCount = 0
    )

    @Test
    fun `remote failure is returned as MediatorResult Error unchanged`() = runBlocking {
        val expected = DomainErrorException(AppError.NoInternet)
        val mediator = mediator(
            remote = FakeRemote { Result.failure(expected) }
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is MediatorResult.Error)
        assertSame(expected, (result as MediatorResult.Error).throwable)
        assertSame(AppError.NoInternet, (result.throwable as DomainErrorException).error)
    }

    @Test
    fun `database failure is converted to DomainErrorException`() = runBlocking {
        val mediator = mediator(
            remote = FakeRemote { Result.success(emptyPage()) },
            local = FakeLocal { throw IllegalStateException("database down") }
        )

        val result = mediator.load(LoadType.REFRESH, pagingState)

        assertTrue(result is MediatorResult.Error)
        val error = (result as MediatorResult.Error).throwable
        assertTrue(error is DomainErrorException)
        assertTrue((error as DomainErrorException).error is AppError.Unknown)
    }

    @Test
    fun `cancellation from remote is rethrown`() {
        val mediator = mediator(
            remote = FakeRemote { throw CancellationException("cancelled") }
        )

        assertThrows(CancellationException::class.java) {
            runBlocking { mediator.load(LoadType.REFRESH, pagingState) }
        }
    }

    @Test
    fun `cancellation while saving is rethrown`() {
        val mediator = mediator(
            remote = FakeRemote { Result.success(emptyPage()) },
            local = FakeLocal { throw CancellationException("cancelled") }
        )

        assertThrows(CancellationException::class.java) {
            runBlocking { mediator.load(LoadType.REFRESH, pagingState) }
        }
    }

    private fun mediator(
        remote: MovesRemoteDataSource,
        local: MoviesLocalDataSource = FakeLocal()
    ) = MovieRemoteMediator(
        remoteDataSource = remote,
        localDataSource = local,
        entityMapper = MovieEntityMapper()
    )

    private fun emptyPage() = BasePagingResponse<MovieResponse>(
        page = 1,
        totalPages = 1,
        results = emptyList()
    )

    private class FakeRemote(
        private val result: () -> Result<BasePagingResponse<MovieResponse>>
    ) : MovesRemoteDataSource {
        override suspend fun getMovies(page: Int): Result<BasePagingResponse<MovieResponse>> = result()
    }

    private class FakeLocal(
        private val onSave: suspend () -> Unit = {}
    ) : MoviesLocalDataSource {
        override fun getMovies(): PagingSource<Int, MovieEntity> = error("Not used by the mediator")

        override fun getMovieById(movieId: Int): Flow<MovieEntity?> = emptyFlow()

        override suspend fun hasMovies(): Boolean = false

        override suspend fun saveMovies(movies: List<MovieEntity>, clearFirst: Boolean) = onSave()

        override suspend fun clearMovies() = Unit
    }
}
