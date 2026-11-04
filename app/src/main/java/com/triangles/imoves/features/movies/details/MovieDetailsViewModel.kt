package com.triangles.imoves.features.movies.details

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.triangles.domain.core.error.AppError
import com.triangles.domain.core.error.DomainErrorException
import com.triangles.domain.features.moves.repository.MoviesRepository
import com.triangles.imoves.base.state.BaseViewModel
import com.triangles.imoves.features.movies.details.contract.MovieDetailsEffect
import com.triangles.imoves.features.movies.details.contract.MovieDetailsEvent
import com.triangles.imoves.features.movies.details.contract.MovieDetailsUiState
import com.triangles.imoves.features.navigation.MovieDetailsRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class MovieDetailsViewModel @Inject constructor(
    private val repository: MoviesRepository,
    private val savedStateHandle: SavedStateHandle,
) : BaseViewModel<MovieDetailsUiState, MovieDetailsEffect, MovieDetailsEvent>(savedStateHandle) {

    init {
        load()
    }

    override fun initialState(savedStateHandle: SavedStateHandle) = MovieDetailsUiState()

    override fun reduceEvent(event: MovieDetailsEvent) {
        when (event) {
            MovieDetailsEvent.OnBack ->
                sendEffect(MovieDetailsEffect.NavigateBack)

            MovieDetailsEvent.OnRetry -> load()
        }
    }

    private fun load() {
        val movieId = savedStateHandle.toRoute<MovieDetailsRoute>().movieId

        execute(
            fullScreen = true,
            call = {
                val details = repository.getMovieById(movieId).first()

                details?.let { Result.success(it) }
                    ?: Result.failure(
                        DomainErrorException(AppError.NotFound)
                    )
            },
            onSuccess = { details ->
                updateState { copy(details = details) }
            }
        )
    }


}
