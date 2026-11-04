package com.triangles.imoves.base.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.triangles.domain.core.error.AppError
import com.triangles.imoves.R

@Composable
fun StateBox(
    state: ScreenState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    loadingContent: @Composable BoxScope.() -> Unit = { CircularProgressIndicator() },
    emptyContent: @Composable BoxScope.() -> Unit = { DefaultEmptyContent() },
    errorContent: @Composable BoxScope.(error: AppError, onRetry: () -> Unit) -> Unit =
        { error, retry -> DefaultErrorContent(error, retry) },
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (state) {
            ScreenState.Loading -> loadingContent()
            ScreenState.Empty -> emptyContent()
            is ScreenState.Error -> errorContent(state.error, onRetry)
            ScreenState.Content -> content()
        }
    }
}

@Composable
internal fun DefaultErrorContent(
    error: AppError,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(error.toMessageRes()),
            textAlign = TextAlign.Center,
        )
        if (error.isRetryable) {
            TextButton(onClick = onRetry) {
                Text(text = stringResource(R.string.action_retry))
            }
        }
    }
}

@Composable
internal fun DefaultEmptyContent() {
    Text(
        text = stringResource(R.string.empty_content),
        modifier = Modifier.padding(24.dp),
        textAlign = TextAlign.Center,
    )
}
