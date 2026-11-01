package com.triangles.domain.core.error

/**
 * Carries an [AppError] through [Result], [androidx.paging.RemoteMediator.MediatorResult.Error]
 * and Paging's `LoadState.Error`. Instances must never wrap another [DomainErrorException].
 */
class DomainErrorException(val error: AppError) : Exception(error.toString())
