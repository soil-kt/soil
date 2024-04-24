// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.internal.epoch

interface QueryModel<out T> {
    val data: T?
    val dataUpdatedAt: Long
    val dataStaleAt: Long
    val error: Throwable?
    val errorUpdatedAt: Long
    val status: QueryStatus
    val fetchStatus: QueryFetchStatus
    val isInvalidated: Boolean
    val isPlaceholderData: Boolean

    val revision: String get() = "d-$dataUpdatedAt/e-$errorUpdatedAt"
    val isPending: Boolean get() = status == QueryStatus.Pending
    val isSuccess: Boolean get() = status == QueryStatus.Success
    val isFailure: Boolean get() = status == QueryStatus.Failure

    fun isStaled(currentAt: Long = epoch()): Boolean {
        return dataStaleAt < currentAt
    }

    fun isPaused(currentAt: Long = epoch()): Boolean {
        return when (val value = fetchStatus) {
            is QueryFetchStatus.Paused -> value.unpauseAt > currentAt
            is QueryFetchStatus.Idle,
            is QueryFetchStatus.Fetching -> false
        }
    }
}

enum class QueryStatus {
    Pending,
    Success,
    Failure
}

sealed class QueryFetchStatus {
    data object Idle : QueryFetchStatus()
    data object Fetching : QueryFetchStatus()
    data class Paused(
        val unpauseAt: Long
    ) : QueryFetchStatus()
}
