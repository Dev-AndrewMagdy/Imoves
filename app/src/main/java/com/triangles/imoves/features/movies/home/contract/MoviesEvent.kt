package com.triangles.imoves.features.movies.home.contract

sealed interface MoviesEvent {
    data class OnQueryChange(val query: String) : MoviesEvent
    data class OnMovieClick(val movieId: Int) : MoviesEvent
}