package com.triangles.imoves.features.movies.home.contract

sealed interface MoviesEffect {
    data class OpenMovie(val movieId: Int) : MoviesEffect
}
