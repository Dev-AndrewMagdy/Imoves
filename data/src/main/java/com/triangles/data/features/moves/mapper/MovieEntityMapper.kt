package com.triangles.data.features.moves.mapper

import com.triangles.data.features.moves.source.local.entity.MovieEntity
import com.triangles.data.features.moves.model.MovieResponse
import com.triangles.data.util.mapper.Mapper
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MovieEntityMapper @Inject constructor() : Mapper<MovieResponse, MovieEntity> {

    override fun map(input: MovieResponse): MovieEntity = MovieEntity(
        id = input.id,
        title = input.title,
        originalTitle = input.originalTitle,
        overview = input.overview,
        posterPath = input.posterPath,
        backdropPath = input.backdropPath,
        releaseDate = input.releaseDate,
        voteAverage = input.voteAverage,
        voteCount = input.voteCount,
        popularity = input.popularity,
        originalLanguage = input.originalLanguage,
        adult = input.adult,
        video = input.video
    )
}
