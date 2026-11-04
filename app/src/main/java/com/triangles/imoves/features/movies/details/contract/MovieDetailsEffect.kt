package com.triangles.imoves.features.movies.details.contract

sealed interface MovieDetailsEffect {
    data object NavigateBack : MovieDetailsEffect
}