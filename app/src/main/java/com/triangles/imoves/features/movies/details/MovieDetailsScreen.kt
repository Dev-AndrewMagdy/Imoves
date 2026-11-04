package com.triangles.imoves.features.movies.details

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.triangles.domain.features.moves.model.MovieDetails
import com.triangles.imoves.base.state.BaseScreen
import com.triangles.imoves.base.state.ScreenState
import com.triangles.imoves.features.common.SystemBarIconsEffect
import com.triangles.imoves.features.movies.details.components.MovieDetailsHeader
import com.triangles.imoves.features.movies.details.components.MovieMetadata
import com.triangles.imoves.features.movies.details.components.MovieOverview
import com.triangles.imoves.features.movies.details.components.MovieSummary
import com.triangles.imoves.features.movies.details.contract.MovieDetailsEffect
import com.triangles.imoves.features.movies.details.contract.MovieDetailsEvent

@Composable
fun MovieDetailsScreen(
    onBack: () -> Unit,
    viewModel: MovieDetailsViewModel = hiltViewModel(),
) {

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    SystemBarIconsEffect(useDarkStatusBarIcons = screenState !is ScreenState.Content)

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                MovieDetailsEffect.NavigateBack -> onBack()
            }
        },
        onRetry = { viewModel.onEvent(MovieDetailsEvent.OnRetry) },

        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { state ->
        state.details?.let {
            MovieDetailsContent(
                details = it,
                onBackClick = { viewModel.onEvent(MovieDetailsEvent.OnBack) },
            )
        }

    }
}

@Composable
private fun MovieDetailsContent(
    details: MovieDetails,
    onBackClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val darkIconsThreshold = remember(density) { with(density) { 160.dp.roundToPx() } }
    val useDarkStatusBarIcons by remember(listState, darkIconsThreshold) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                    listState.firstVisibleItemScrollOffset > darkIconsThreshold
        }
    }
    SystemBarIconsEffect(useDarkStatusBarIcons = useDarkStatusBarIcons)

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
    ) {
        item(key = "header") {
            MovieDetailsHeader(
                backdropImage = details.backdropImage,
                onBackClick = onBackClick,
            )
        }

        item(key = "summary") {
            MovieSummary(
                details = details,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        item(key = "metadata") {
            MovieMetadata(
                details = details,
                modifier = Modifier.padding(top = 20.dp),
            )
        }

        item(key = "overview") {
            MovieOverview(
                overview = details.overview,
                modifier = Modifier.padding(top = 20.dp, bottom = 32.dp),
            )
        }
    }
}
