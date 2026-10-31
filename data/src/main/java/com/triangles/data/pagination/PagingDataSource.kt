package com.triangles.data.pagination

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.triangles.data.network.response.BasePagingResponse
import kotlinx.coroutines.CancellationException

class PagingDataSource<T : Any>(
    private val fetchPage: suspend (page: Int) -> Result<BasePagingResponse<T>>,
) : PagingSource<Int, T>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val page = params.key ?: STARTING_PAGE

        return try {
            fetchPage(page).fold(
                onSuccess = { it.toLoadResult(page) },
                onFailure = { error ->
                    if (error is CancellationException) throw error
                    LoadResult.Error(error)
                },
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition
            ?.let(state::closestPageToPosition)
            ?.let { it.prevKey?.plus(1) ?: it.nextKey?.minus(1) }

    private fun BasePagingResponse<T>.toLoadResult(page: Int): LoadResult<Int, T> {
        val totalPages = totalPages
        val items = results
        return LoadResult.Page(
            data = items,
            prevKey = (page - 1).takeIf { page > STARTING_PAGE },
            nextKey = (page + 1).takeIf { items.isNotEmpty() && page < totalPages },
        )
    }

    private companion object {
        const val STARTING_PAGE = 1
    }
}