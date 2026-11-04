package com.triangles.imoves.features.navigation

import kotlinx.serialization.Serializable

@Serializable
data object DiscoverRoute

@Serializable
data class MovieDetailsRoute(
    val movieId: Int,
)
