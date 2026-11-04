package com.triangles.imoves.features.navigation

interface Navigator {

    fun navigate(destination: Any)

    fun navigateInclusive(destination: Any)

    fun navigateIfNotCurrent(destination: Any)

    fun navigateSingleTopTo(destination: Any)

    fun navigateAndReplaceStartRoute(destination: Any)

    fun popBackStack(): Boolean

    fun canGoBack(): Boolean

    fun isCurrent(destination: Any): Boolean
}
