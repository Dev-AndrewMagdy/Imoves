package com.triangles.data.features.moves.source.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import com.triangles.data.features.moves.source.local.entity.RemotePageState
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query(
        """
        SELECT * FROM movies
        WHERE :query = '' OR LOWER(title) LIKE '%' || LOWER(:query) || '%'
        ORDER BY popularity DESC
        """
    )
    fun getMovies(query: String): PagingSource<Int, MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    fun getMovieById(movieId: Int): Flow<MovieEntity?>
    @Query("SELECT EXISTS(SELECT 1 FROM movies)")
    suspend fun hasMovies(): Boolean
    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)
    @Query("DELETE FROM movies")
    suspend fun clearMovies()

    @Query("SELECT * FROM remote_page_state WHERE id = 0 LIMIT 1")
    suspend fun getRemotePageState(): RemotePageState?

    @Upsert
    suspend fun upsertRemotePageState(state: RemotePageState)

    @Query("DELETE FROM remote_page_state")
    suspend fun clearRemotePageState()
}
