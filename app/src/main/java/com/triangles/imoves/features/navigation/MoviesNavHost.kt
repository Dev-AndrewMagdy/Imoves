package com.triangles.imoves.features.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.triangles.imoves.features.movies.details.MovieDetailsScreen
import com.triangles.imoves.features.movies.home.MoviesRoute

@Composable
fun MoviesNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val navigator = remember(navController) { AppNavigator(navController) }

    CompositionLocalProvider(
        LocalNavigator provides navigator,
        LocalRootNavigator provides navigator,
    ) {
        NavHost(
            navController = navController,
            startDestination = DiscoverRoute,
            modifier = modifier.fillMaxSize(),
        ) {
            composable<DiscoverRoute> {
                MoviesRoute(
                    onOpenMovie = { movieId ->
                        navigator.navigateSingleTopTo(MovieDetailsRoute(movieId))
                    }
                )
            }

            composable<MovieDetailsRoute> {
                MovieDetailsScreen(onBack = { navigator.popBackStack() })
            }
        }
    }
}
