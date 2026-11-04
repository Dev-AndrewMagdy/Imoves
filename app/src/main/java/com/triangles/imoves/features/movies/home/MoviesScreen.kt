package com.triangles.imoves.features.movies.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.triangles.domain.features.moves.model.Movie
import com.triangles.imoves.R
import com.triangles.imoves.base.state.BasePagingList
import com.triangles.imoves.base.state.BaseScreen
import com.triangles.imoves.features.common.SystemBarIconsEffect
import com.triangles.imoves.features.movies.home.components.MovieCard
import com.triangles.imoves.features.movies.home.components.MoviesSearchBar
import com.triangles.imoves.features.movies.home.components.MoviesTopBar
import com.triangles.imoves.features.movies.home.contract.MoviesEffect
import com.triangles.imoves.features.movies.home.contract.MoviesEvent

@Composable
fun MoviesRoute(
    onOpenMovie: (Int) -> Unit,
    viewModel: MoviesViewModel = hiltViewModel(),
) {
    val movies = viewModel.movies.collectAsLazyPagingItems()

    MoviesScreen(
        viewModel = viewModel,
        movies = movies,
        onOpenMovie = onOpenMovie,
    )
}

@Composable
private fun MoviesScreen(
    viewModel: MoviesViewModel,
    movies: LazyPagingItems<Movie>,
    onOpenMovie: (Int) -> Unit,
) {
    SystemBarIconsEffect(useDarkStatusBarIcons = true)

    BaseScreen(
        viewModel = viewModel,
        onEffect = { effect ->
            when (effect) {
                is MoviesEffect.OpenMovie -> onOpenMovie(effect.movieId)
            }
        },
        onRetry = movies::retry,
        topBar = { MoviesTopBar() },
    ) { state ->
        Column(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.discover_title),
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.discover_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(10.dp))
                MoviesSearchBar(
                    query = state.query,
                    onQueryChange = { query ->
                        viewModel.onEvent(MoviesEvent.OnQueryChange(query))
                    },
                    onClearQuery = {
                        viewModel.onEvent(MoviesEvent.OnQueryChange(""))
                    },
                )
                Spacer(modifier = Modifier.height(10.dp))
                MoviesSectionHeader(movieCount = movies.itemCount)
            }

            BasePagingList(
                items = movies,
                modifier = Modifier.weight(1f),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    end = 20.dp,
                    top = 4.dp,
                    bottom = 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                itemKey = { movie -> movie.id },
                emptyContent = { MoviesEmptyContent(query = state.query) },
                itemContent = { movie ->
                    MovieCard(
                        movie = movie,
                        onClick = {
                            viewModel.onEvent(MoviesEvent.OnMovieClick(movie.id))
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun MoviesSectionHeader(movieCount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.popular_movies),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (movieCount > 0) {
            Text(
                text = pluralStringResource(R.plurals.movies_count, movieCount, movieCount),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MoviesEmptyContent(query: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = if (query.isBlank()) {
                    stringResource(R.string.no_movies_title)
                } else {
                    stringResource(R.string.no_movies_match_title)
                },
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
            )
            Text(
                text = if (query.isBlank()) {
                    stringResource(R.string.no_movies_message)
                } else {
                    stringResource(R.string.no_movies_match_message)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
