// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.DataModel
import soil.query.core.epoch
import soil.query.core.isNone

/**
 * Data model for the state handled by [QueryKey] or [InfiniteQueryKey].
 *
 * All data models related to queries, implement this interface.
 *
 * @param T Type of data to retrieve.
 */
interface QueryModel<out T> : DataModel<T> {

    /**
     * The timestamp when the data is considered stale.
     */
    val staleAt: Long

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
     * The revision of the currently snapshot.
     */
    val revision: String get() = "d-$replyUpdatedAt/e-$errorUpdatedAt"

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
     * Returns `true` if the query is fetching, `false` otherwise.
     */
    val isFetching: Boolean get() = fetchStatus == QueryFetchStatus.Fetching

    /**
     * Returns `true` if the query is staled, `false` otherwise.
     */
    fun isStaled(currentAt: Long = epoch()): Boolean {
        return staleAt < currentAt
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

    /**
     * Returns true if the [QueryModel] is awaited.
     *
     * @see DataModel.isAwaited
     */
    override fun isAwaited(): Boolean {
        return isPending
            || (isFailure && isFetching && reply.isNone)
            || (isInvalidated && isFetching)
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
