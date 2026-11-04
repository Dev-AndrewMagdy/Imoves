package com.triangles.domain.features.moves.model

data class MovieDetails(
    val id: Int,
    val title: String,
    val originalTitle: String,
    val overview: String,
    val posterImage: String,
    val backdropImage: String,
    val releaseDate: String,
    val voteAverage: Double,
    val voteCount: Int,
    val popularity: Double,
    val originalLanguage: String,
    val adult: Boolean,
    val video: Boolean
)
