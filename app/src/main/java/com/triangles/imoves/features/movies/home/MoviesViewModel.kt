package com.triangles.imoves.features.movies.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.triangles.domain.features.moves.model.Movie
import com.triangles.domain.features.moves.usecase.GetMoviesUseCase
import com.triangles.imoves.base.state.BaseViewModel
import com.triangles.imoves.features.movies.home.contract.MoviesEffect
import com.triangles.imoves.features.movies.home.contract.MoviesEvent
import com.triangles.imoves.features.movies.home.contract.MoviesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class MoviesViewModel @Inject constructor(
    private val getMoviesUseCase: GetMoviesUseCase,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<MoviesUiState, MoviesEffect, MoviesEvent>(savedStateHandle) {

    private companion object {
        const val SEARCH_DEBOUNCE_MS = 350L
    }

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val movies: Flow<PagingData<Movie>> = uiState
        .map { it.query.trim() }
        .debounce { query ->
            if (query.isEmpty()) 0L else SEARCH_DEBOUNCE_MS
        }
        .distinctUntilChanged()
        .flatMapLatest { query -> getMoviesUseCase(query) }
        .cachedIn(viewModelScope)

    override fun initialState(savedStateHandle: SavedStateHandle): MoviesUiState =
        MoviesUiState()

    override fun reduceEvent(event: MoviesEvent) {
        when (event) {
            is MoviesEvent.OnQueryChange ->
                updateState { copy(query = event.query) }

            is MoviesEvent.OnMovieClick ->
                sendEffect(MoviesEffect.OpenMovie(event.movieId))
        }
    }
}
