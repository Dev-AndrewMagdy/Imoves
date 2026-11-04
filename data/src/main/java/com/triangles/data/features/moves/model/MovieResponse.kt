package com.triangles.data.features.moves.model

import com.google.gson.annotations.SerializedName

data class MovieResponse(
    val id: Int,

    val title: String,

    @SerializedName("original_title")
    val originalTitle: String? = null,

    val overview: String? = null,

    @SerializedName("poster_path")
    val posterPath: String? = null,

    @SerializedName("backdrop_path")
    val backdropPath: String? = null,

    @SerializedName("release_date")
    val releaseDate: String? = null,

    @SerializedName("vote_average")
    val voteAverage: Double = 0.0,

    @SerializedName("vote_count")
    val voteCount: Int = 0,

    val popularity: Double = 0.0,

    @SerializedName("original_language")
    val originalLanguage: String? = null,

    val adult: Boolean = false,

    val video: Boolean = false
)