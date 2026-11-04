package com.triangles.imoves.features.movies.details.contract

sealed interface MovieDetailsEvent {
    data object OnBack : MovieDetailsEvent
    data object OnRetry : MovieDetailsEvent
}
