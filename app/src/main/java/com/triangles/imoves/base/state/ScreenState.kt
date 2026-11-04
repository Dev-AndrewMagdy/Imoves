package com.triangles.imoves.base.state

import com.triangles.domain.core.error.AppError

sealed interface ScreenState {
    data object Loading : ScreenState
    data object Empty : ScreenState
    data object Content : ScreenState
    data class Error(val error: AppError) : ScreenState
}
