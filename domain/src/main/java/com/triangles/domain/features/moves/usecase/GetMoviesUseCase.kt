package com.triangles.domain.features.moves.usecase

import androidx.paging.PagingData
import com.triangles.domain.features.moves.model.Movie
import kotlinx.coroutines.flow.Flow

fun interface GetMoviesUseCase {
    operator fun invoke(query: String): Flow<PagingData<Movie>>
}
