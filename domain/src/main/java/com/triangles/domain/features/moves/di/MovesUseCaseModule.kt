package com.triangles.domain.features.moves.di

import com.triangles.domain.features.moves.repository.MoviesRepository
import com.triangles.domain.features.moves.usecase.GetMoviesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object MovesUseCaseModule {

    @Provides
    fun provideGetMoviesUseCase(repository: MoviesRepository): GetMoviesUseCase =
        GetMoviesUseCase { query -> repository.getMoves(query) }
}
