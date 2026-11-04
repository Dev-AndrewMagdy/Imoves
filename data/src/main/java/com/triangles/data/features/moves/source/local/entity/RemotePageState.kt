package com.triangles.data.features.moves.source.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Persists how far the remote feed has been paged.
 *
 * The [androidx.paging.PagingState] passed to a [androidx.paging.RemoteMediator] can be stale (for
 * example Paging queues an APPEND right after an empty local refresh), so the mediator must not
 * derive the next remote page from it. This single row is the source of truth and also survives
 * process death.
 */
@Entity(tableName = "remote_page_state")
data class RemotePageState(
    @PrimaryKey
    val id: Int = SINGLETON_ID,
    val nextPage: Int,
    val reachedEnd: Boolean
) {
    companion object {
        const val SINGLETON_ID = 0
    }
}
