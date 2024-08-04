// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.epoch

/**
 * Data model for the state handled by [QueryKey] or [InfiniteQueryKey].
 *
 * All data models related to queries, implement this interface.
 *
 * @param T Type of data to retrieve.
 */
interface QueryModel<out T> {

    /**
     * The return value from the query.
     */
    val data: T?

    /**
     * The timestamp when the data was updated.
     */
    val dataUpdatedAt: Long

    /**
     * The timestamp when the data is considered stale.
     */
    val dataStaleAt: Long

    /**
     * The error that occurred.
     */
    val error: Throwable?

    /**
     * The timestamp when the error occurred.
     */
    val errorUpdatedAt: Long

    /**
     * The status of the query.
     */
    val status: QueryStatus

    /**
     * The fetch status of the query.
     */
    val fetchStatus: QueryFetchStatus

    /**
     * Returns `true` if the query is invalidated, `false` otherwise.
     */
    val isInvalidated: Boolean

    /**
     * Returns `true` if the query is placeholder data, `false` otherwise.
     */
    val isPlaceholderData: Boolean

    /**
     * The revision of the currently snapshot.
     */
    val revision: String get() = "d-$dataUpdatedAt/e-$errorUpdatedAt"

    /**
     * Returns `true` if the query is pending, `false` otherwise.
     */
    val isPending: Boolean get() = status == QueryStatus.Pending

    /**
     * Returns `true` if the query is successful, `false` otherwise.
     */
    val isSuccess: Boolean get() = status == QueryStatus.Success

    /**
     * Returns `true` if the query is a failure, `false` otherwise.
     */
    val isFailure: Boolean get() = status == QueryStatus.Failure

    /**
     * Returns `true` if the query is staled, `false` otherwise.
     */
    fun isStaled(currentAt: Long = epoch()): Boolean {
        return dataStaleAt < currentAt
    }

    /**
     * Returns `true` if the query is paused, `false` otherwise.
     */
    fun isPaused(currentAt: Long = epoch()): Boolean {
        return when (val value = fetchStatus) {
            is QueryFetchStatus.Paused -> value.unpauseAt > currentAt
            is QueryFetchStatus.Idle,
            is QueryFetchStatus.Fetching -> false
        }
    }
}

/**
 * The status of the query.
 */
enum class QueryStatus {
    Pending,
    Success,
    Failure
}

/**
 * The fetch status of the query.
 */
sealed class QueryFetchStatus {

    data object Idle : QueryFetchStatus()
    data object Fetching : QueryFetchStatus()
    data class Paused(
        val unpauseAt: Long
    ) : QueryFetchStatus()
}
