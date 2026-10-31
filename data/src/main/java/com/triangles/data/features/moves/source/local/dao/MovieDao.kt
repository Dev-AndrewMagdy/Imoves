package com.triangles.data.features.moves.source.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies ORDER BY popularity DESC")
    fun getMovies(): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    fun getMovieById(movieId: Int): Flow<MovieEntity?>
    @Query("SELECT EXISTS(SELECT 1 FROM movies)")
    suspend fun hasMovies(): Boolean
    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)
    @Query("DELETE FROM movies")
    suspend fun clearMovies()
}
