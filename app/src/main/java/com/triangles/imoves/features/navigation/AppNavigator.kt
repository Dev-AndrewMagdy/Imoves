package com.triangles.imoves.features.navigation

import android.annotation.SuppressLint
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("Navigator is not provided")
}

val LocalRootNavigator = staticCompositionLocalOf<Navigator> {
    error("Root Navigator is not provided")
}

@SuppressLint("RestrictedApi")
class AppNavigator(
    private val navController: NavHostController
) : Navigator {

    override fun navigate(destination: Any) {
        navController.navigate(destination)
    }

    override fun navigateInclusive(destination: Any) {
        val currentDestinationId = navController.currentDestination?.id
        if (currentDestinationId == null) {
            navController.navigate(destination)
            return
        }

        navController.navigate(destination) {
            popUpTo(currentDestinationId) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }

    override fun navigateIfNotCurrent(destination: Any) {
        if (navController.currentDestination?.hasRoute(destination::class) != true) {
            navController.navigate(destination)
        }
    }

    override fun navigateSingleTopTo(destination: Any) {
        if (navController.currentDestination?.hasRoute(destination::class) == true) {
            return
        }

        navController.navigate(destination) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    override fun navigateAndReplaceStartRoute(destination: Any) {
        navController.popBackStack(
            navController.graph.startDestinationRoute ?: "",
            true
        )

        navController.graph.setStartDestination(destination)
        navController.navigate(destination)
    }

    override fun popBackStack(): Boolean {
        return navController.popBackStack()
    }

    override fun canGoBack(): Boolean {
        val currentDestination =
            navController.currentBackStackEntry?.destination

        return currentDestination
            ?.hierarchy
            ?.any {
                it.route == navController.graph.startDestinationRoute
            }
            ?.not() == true
    }

    override fun isCurrent(destination: Any): Boolean {
        return navController.currentBackStackEntry
            ?.destination
            ?.hasRoute(destination::class) == true
    }
}
