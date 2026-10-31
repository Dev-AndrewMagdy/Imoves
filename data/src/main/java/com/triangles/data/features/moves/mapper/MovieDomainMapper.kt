package com.triangles.data.features.moves.mapper

import com.triangles.data.features.moves.source.local.entity.MovieEntity
import com.triangles.data.util.mapper.Mapper
import com.triangles.domain.features.moves.model.Movie
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieDomainMapper @Inject constructor() : Mapper<MovieEntity, Movie> {

    override fun map(input: MovieEntity): Movie = Movie(
        id = input.id,
        title = input.title,
        originalTitle = input.originalTitle.orEmpty(),
        overview = input.overview.orEmpty(),
        posterPath = input.posterPath.orEmpty(),
        backdropPath = input.backdropPath.orEmpty(),
        releaseDate = input.releaseDate.orEmpty(),
        voteAverage = input.voteAverage,
        voteCount = input.voteCount,
        popularity = input.popularity,
        originalLanguage = input.originalLanguage.orEmpty(),
        adult = input.adult,
        video = input.video
    )
}
