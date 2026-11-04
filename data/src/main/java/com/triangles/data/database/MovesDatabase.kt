package com.triangles.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.triangles.data.features.moves.source.local.dao.MovieDao
import com.triangles.data.features.moves.source.local.entity.MovieEntity
import com.triangles.data.features.moves.source.local.entity.RemotePageState

@Database(
    entities = [MovieEntity::class, RemotePageState::class],
    version = 4
)
abstract class MovesDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object {
        const val NAME = "moves.db"
    }
}
