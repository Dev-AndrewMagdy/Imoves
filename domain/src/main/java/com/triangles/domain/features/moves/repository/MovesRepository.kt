package com.triangles.domain.features.moves.repository

import androidx.paging.PagingData
import com.triangles.domain.features.moves.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovesRepository {

    fun getMovies(): Flow<PagingData<Movie>>

}
