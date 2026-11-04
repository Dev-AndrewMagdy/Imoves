package com.triangles.domain.features.moves.usecase

import androidx.paging.PagingData
import com.triangles.domain.features.moves.di.MovesUseCaseModule
import com.triangles.domain.features.moves.model.Movie
import com.triangles.domain.features.moves.model.MovieDetails
import com.triangles.domain.features.moves.repository.MoviesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class GetMoviesUseCaseTest {

    private val repository = FakeMoviesRepository()
    private val useCase = MovesUseCaseModule.provideGetMoviesUseCase(repository)

    @Test
    fun `invoke forwards the query to the repository`() {
        useCase("batman")

        assertEquals(listOf("batman"), repository.requestedQueries)
    }

    @Test
    fun `invoke forwards an empty query to the repository`() {
        useCase("")

        assertEquals(listOf(""), repository.requestedQueries)
    }

    @Test
    fun `invoke returns the repository flow unchanged`() {
        val expected = flowOf(PagingData.empty<Movie>())
        repository.moviesFlow = expected

        assertSame(expected, useCase("dune"))
    }

    private class FakeMoviesRepository(
        var moviesFlow: Flow<PagingData<Movie>> = flowOf(PagingData.empty()),
    ) : MoviesRepository {

        val requestedQueries = mutableListOf<String>()

        override fun getMoves(query: String): Flow<PagingData<Movie>> {
            requestedQueries += query
            return moviesFlow
        }

        override fun getMovieById(movieId: Int): Flow<MovieDetails?> = emptyFlow()
    }
}
