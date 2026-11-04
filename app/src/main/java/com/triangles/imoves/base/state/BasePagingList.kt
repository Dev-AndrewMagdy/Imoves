package com.triangles.imoves.base.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey as pagingItemKey
import com.triangles.domain.core.error.AppError
import com.triangles.imoves.R


fun <T : Any> LazyPagingItems<T>.toScreenState(): ScreenState {
    val refresh = loadState.refresh
    val append = loadState.append

    return when {
        itemCount > 0 -> ScreenState.Content

        refresh is LoadState.Loading -> ScreenState.Loading

        refresh is LoadState.Error -> ScreenState.Error(refresh.error.asAppError())

        append is LoadState.NotLoading && append.endOfPaginationReached -> ScreenState.Empty

        else -> ScreenState.Loading
    }
}

@Composable
fun <T : Any> BasePagingList(
    items: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    columns: GridCells = GridCells.Fixed(1),
    contentPadding: PaddingValues = PaddingValues(),
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    lazyGridState: LazyGridState = rememberLazyGridState(),
    itemKey: ((item: T) -> Any)? = null,
    itemContentType: ((item: T) -> Any?)? = null,
    itemSpan: (LazyGridItemSpanScope.(item: T) -> GridItemSpan)? = null,
    emptyContent: @Composable BoxScope.() -> Unit = { DefaultEmptyContent() },
    loadingContent: @Composable BoxScope.() -> Unit = { CircularProgressIndicator() },
    appendLoadingContent: @Composable () -> Unit = { AppendLoading() },
    itemContent: @Composable LazyGridItemScope.(item: T) -> Unit,
) {
    val span: (LazyGridItemSpanScope.(Int) -> GridItemSpan)? = itemSpan?.let { spanOf ->
        { index: Int -> items.peek(index)?.let { spanOf(it) } ?: GridItemSpan(1) }
    }
    val refreshError = items.loadState.refresh as? LoadState.Error

    StateBox(
        state = items.toScreenState(),
        onRetry = items::retry,
        modifier = modifier,
        loadingContent = loadingContent,
        emptyContent = emptyContent,
    ) {
        LazyVerticalGrid(
            columns = columns,
            modifier = Modifier.fillMaxSize(),
            state = lazyGridState,
            contentPadding = contentPadding,
            verticalArrangement = verticalArrangement,
            horizontalArrangement = horizontalArrangement,
        ) {
            if (refreshError != null) {
                item(span = FullSpan) {
                    PagingErrorFooter(
                        error = refreshError.error.asAppError(),
                        onRetry = items::retry,
                    )
                }
            }

            items(
                count = items.itemCount,
                key = items.pagingItemKey(itemKey),
                span = span,
                contentType = items.itemContentType(itemContentType ?: { null }),
            ) { index ->
                items[index]?.let { itemContent(it) }
            }

            appendStateItem(
                state = items.loadState.append,
                loadingContent = appendLoadingContent,
                onRetry = items::retry,
            )
        }
    }
}

private val FullSpan: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

private fun LazyGridScope.appendStateItem(
    state: LoadState,
    loadingContent: @Composable () -> Unit,
    onRetry: () -> Unit,
) {
    when (state) {
        is LoadState.Loading -> item(span = FullSpan) { loadingContent() }
        is LoadState.Error -> item(span = FullSpan) {
            PagingErrorFooter(error = state.error.asAppError(), onRetry = onRetry)
        }
        is LoadState.NotLoading -> Unit
    }
}

@Composable
private fun AppendLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun PagingErrorFooter(
    error: AppError,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = stringResource(error.toMessageRes()))
        if (error.isRetryable) {
            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.action_retry))
            }
        }
    }
}
