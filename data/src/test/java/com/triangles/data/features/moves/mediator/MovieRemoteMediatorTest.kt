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
import com.triangles.data.features.moves.source.local.entity.RemotePageState
import com.triangles.data.features.moves.source.remote.MovesRemoteDataSource
import com.triangles.data.network.response.BasePagingResponse
import com.triangles.domain.core.error.AppError
import com.triangles.domain.core.error.DomainErrorException
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalPagingApi::class)
class MovieRemoteMediatorTest {

    private val emptyPagingState = PagingState<Int, MovieEntity>(
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

        val result = mediator.load(LoadType.REFRESH, emptyPagingState)

        assertTrue(result is MediatorResult.Error)
        assertSame(expected, (result as MediatorResult.Error).throwable)
        assertSame(AppError.NoInternet, (result.throwable as DomainErrorException).error)
    }

    @Test
    fun `database failure is converted to DomainErrorException`() = runBlocking {
        val mediator = mediator(
            remote = FakeRemote { Result.success(emptyPage()) },
            local = FakeLocal(onSave = { throw IllegalStateException("database down") })
        )

        val result = mediator.load(LoadType.REFRESH, emptyPagingState)

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
            runBlocking { mediator.load(LoadType.REFRESH, emptyPagingState) }
        }
    }

    @Test
    fun `cancellation while saving is rethrown`() {
        val mediator = mediator(
            remote = FakeRemote { Result.success(emptyPage()) },
            local = FakeLocal(onSave = { throw CancellationException("cancelled") })
        )

        assertThrows(CancellationException::class.java) {
            runBlocking { mediator.load(LoadType.REFRESH, emptyPagingState) }
        }
    }

    @Test
    fun `refresh persists the next page`() = runBlocking {
        val local = FakeLocal()
        val mediator = mediator(
            remote = FakeRemote {
                Result.success(page(page = 1, totalPages = 3, ids = listOf(10, 11)))
            },
            local = local
        )

        val result = mediator.load(LoadType.REFRESH, emptyPagingState)

        assertSuccess(endOfPaginationReached = false, result)
        assertEquals(RemotePageState(nextPage = 2, reachedEnd = false), local.savedPageState)
    }

    @Test
    fun `refresh on the last page persists that pagination ended`() = runBlocking {
        val local = FakeLocal()
        val mediator = mediator(
            remote = FakeRemote {
                Result.success(page(page = 1, totalPages = 1, ids = listOf(10)))
            },
            local = local
        )

        val result = mediator.load(LoadType.REFRESH, emptyPagingState)

        assertSuccess(endOfPaginationReached = true, result)
        assertEquals(RemotePageState(nextPage = 1, reachedEnd = true), local.savedPageState)
    }

    @Test
    fun `append uses the persisted page even when paging state is stale and empty`() = runBlocking {
        // This reproduces the first-scroll bug: Paging queues an APPEND whose PagingState was
        // captured before the local refresh populated Room, so it has no pages/items.
        val local = FakeLocal(pageState = RemotePageState(nextPage = 2, reachedEnd = false))
        val remote = FakeRemote {
            Result.success(page(page = 2, totalPages = 10, ids = listOf(20)))
        }
        val mediator = mediator(remote = remote, local = local)

        val result = mediator.load(LoadType.APPEND, emptyPagingState)

        assertEquals(listOf(2), remote.requestedPages)
        assertSuccess(endOfPaginationReached = false, result)
        assertEquals(RemotePageState(nextPage = 3, reachedEnd = false), local.savedPageState)
    }

    @Test
    fun `append marks the feed complete when the final page is fetched`() = runBlocking {
        val local = FakeLocal(pageState = RemotePageState(nextPage = 10, reachedEnd = false))
        val remote = FakeRemote {
            Result.success(page(page = 10, totalPages = 10, ids = listOf(100)))
        }
        val mediator = mediator(remote = remote, local = local)

        val result = mediator.load(LoadType.APPEND, emptyPagingState)

        assertSuccess(endOfPaginationReached = true, result)
        assertEquals(RemotePageState(nextPage = 10, reachedEnd = true), local.savedPageState)
    }

    @Test
    fun `append stops when the persisted state reached the end`() = runBlocking {
        val remote = FakeRemote { error("must not be called") }
        val local = FakeLocal(pageState = RemotePageState(nextPage = 5, reachedEnd = true))
        val mediator = mediator(remote = remote, local = local)

        val result = mediator.load(LoadType.APPEND, emptyPagingState)

        assertSuccess(endOfPaginationReached = true, result)
        assertTrue(remote.requestedPages.isEmpty())
        assertNull(local.savedPageState)
    }

    @Test
    fun `append stops when no persisted page state exists`() = runBlocking {
        val remote = FakeRemote { error("must not be called") }
        val mediator = mediator(remote = remote, local = FakeLocal())

        val result = mediator.load(LoadType.APPEND, emptyPagingState)

        assertSuccess(endOfPaginationReached = true, result)
        assertTrue(remote.requestedPages.isEmpty())
    }

    private fun assertSuccess(endOfPaginationReached: Boolean, result: MediatorResult) {
        assertTrue(result is MediatorResult.Success)
        assertEquals(
            endOfPaginationReached,
            (result as MediatorResult.Success).endOfPaginationReached
        )
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

    private fun page(page: Int, totalPages: Int, ids: List<Int>) =
        BasePagingResponse<MovieResponse>(
            page = page,
            totalPages = totalPages,
            results = ids.map { id -> MovieResponse(id = id, title = "Movie $id") }
        )

    private class FakeRemote(
        private val result: (page: Int) -> Result<BasePagingResponse<MovieResponse>>
    ) : MovesRemoteDataSource {
        val requestedPages = mutableListOf<Int>()

        override suspend fun getMovies(page: Int): Result<BasePagingResponse<MovieResponse>> {
            requestedPages += page
            return result(page)
        }
    }

    private class FakeLocal(
        private val onSave: suspend () -> Unit = {},
        private val pageState: RemotePageState? = null
    ) : MoviesLocalDataSource {
        var savedPageState: RemotePageState? = null

        override fun getMovies(query: String): PagingSource<Int, MovieEntity> =
            error("Not used by the mediator")

        override fun getMovieById(movieId: Int): Flow<MovieEntity?> = emptyFlow()

        override suspend fun hasMovies(): Boolean = false

        override suspend fun getRemotePageState(): RemotePageState? = pageState

        override suspend fun saveMovies(
            movies: List<MovieEntity>,
            pageState: RemotePageState?,
            clearFirst: Boolean
        ) {
            savedPageState = pageState
            onSave()
        }

        override suspend fun clearMovies() = Unit
    }
}
