package com.triangles.data.features.moves.source.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies ORDER BY popularity DESC")
    fun observeMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies ORDER BY popularity DESC")
    suspend fun getMovies(): List<MovieEntity>

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM movies")
    suspend fun clearMovies()
}
