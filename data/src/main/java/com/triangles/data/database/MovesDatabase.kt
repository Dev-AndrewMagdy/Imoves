package com.triangles.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.triangles.data.features.moves.source.local.dao.MovieDao
import com.triangles.data.features.moves.source.local.entity.MovieEntity

@Database(
    entities = [MovieEntity::class],
    version = 2
)
abstract class MovesDatabase : RoomDatabase() {

    abstract fun movieDao(): MovieDao

    companion object {
        const val NAME = "moves.db"
    }
}
