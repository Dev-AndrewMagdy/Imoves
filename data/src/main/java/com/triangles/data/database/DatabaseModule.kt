package com.triangles.data.database

import android.content.Context
import androidx.room.Room
import com.triangles.data.features.moves.source.local.dao.MovieDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMovesDatabase(@ApplicationContext context: Context): MovesDatabase =
        Room.databaseBuilder(
            context,
            MovesDatabase::class.java,
            MovesDatabase.NAME
        ).build()

    @Provides
    fun provideMovieDao(database: MovesDatabase): MovieDao = database.movieDao()
}
