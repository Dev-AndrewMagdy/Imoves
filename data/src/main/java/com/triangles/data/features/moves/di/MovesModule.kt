package com.triangles.data.features.moves.di

import com.triangles.data.features.moves.repository.MoviesRepositoryImpl
import com.triangles.data.features.moves.source.local.MoviesLocalDataSource
import com.triangles.data.features.moves.source.local.MovesLocalDataSourceImpl
import com.triangles.data.features.moves.source.remote.MovesApi
import com.triangles.data.features.moves.source.remote.MovesRemoteDataSource
import com.triangles.data.features.moves.source.remote.MovesRemoteDataSourceImpl
import com.triangles.domain.features.moves.repository.MoviesRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MovesModule {

    @Binds
    @Singleton
    abstract fun bindMovesRepository(repository: MoviesRepositoryImpl): MoviesRepository

    @Binds
    @Singleton
    abstract fun bindMovesRemoteDataSource(dataSource: MovesRemoteDataSourceImpl): MovesRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindMovesLocalDataSource(dataSource: MovesLocalDataSourceImpl): MoviesLocalDataSource

    companion object {

        @Provides
        @Singleton
        fun provideMovesApi(retrofit: Retrofit): MovesApi =
            retrofit.create(MovesApi::class.java)
    }
}
