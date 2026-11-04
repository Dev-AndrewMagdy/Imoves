package com.triangles.domain.features.moves.repository

import androidx.paging.PagingData
import com.triangles.domain.features.moves.model.Movie
import com.triangles.domain.features.moves.model.MovieDetails
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {

    fun getMoves(query: String = ""): Flow<PagingData<Movie>>

    fun getMovieById(movieId: Int): Flow<MovieDetails?>

}