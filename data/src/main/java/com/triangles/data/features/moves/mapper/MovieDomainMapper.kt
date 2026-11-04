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
        releaseDate = input.releaseDate.orEmpty(),
        posterImage = input.posterPath.orEmpty(),
    )
}
