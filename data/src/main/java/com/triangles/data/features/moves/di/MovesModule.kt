package com.triangles.data.features.moves.di

import com.triangles.data.features.moves.repository.MovesRepositoryImpl
import com.triangles.data.features.moves.source.local.MovesLocalDataSource
import com.triangles.data.features.moves.source.local.MovesLocalDataSourceImpl
import com.triangles.data.features.moves.source.remote.MovesApi
import com.triangles.data.features.moves.source.remote.MovesRemoteDataSource
import com.triangles.data.features.moves.source.remote.MovesRemoteDataSourceImpl
import com.triangles.domain.features.moves.repository.MovesRepository
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
    abstract fun bindMovesRepository(repository: MovesRepositoryImpl): MovesRepository

    @Binds
    @Singleton
    abstract fun bindMovesRemoteDataSource(dataSource: MovesRemoteDataSourceImpl): MovesRemoteDataSource

    @Binds
    @Singleton
    abstract fun bindMovesLocalDataSource(dataSource: MovesLocalDataSourceImpl): MovesLocalDataSource

    companion object {

        @Provides
        @Singleton
        fun provideMovesApi(retrofit: Retrofit): MovesApi =
            retrofit.create(MovesApi::class.java)
    }
}
