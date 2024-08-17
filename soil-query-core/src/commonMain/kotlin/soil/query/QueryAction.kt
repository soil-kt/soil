// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply

/**
 * Query actions are used to update the [query state][QueryState].
 *
 * @param T Type of the return value from the query.
 */
sealed interface QueryAction<out T> {

    /**
     * Indicates that the query is fetching data.
     *
     * @param isInvalidated Indicates whether the query is invalidated.
     */
    data class Fetching(
        val isInvalidated: Boolean? = null
    ) : QueryAction<Nothing>

    /**
     * Indicates that the query is successful.
     *
     * @param data The data to be updated.
     * @param dataUpdatedAt The timestamp when the data was updated.
     * @param dataStaleAt The timestamp when the data becomes stale.
     */
    data class FetchSuccess<T>(
        val data: T,
        val dataUpdatedAt: Long,
        val dataStaleAt: Long
    ) : QueryAction<T>

    /**
     * Indicates that the query has failed.
     *
     * @param error The error that occurred.
     * @param errorUpdatedAt The timestamp when the error occurred.
     * @param paused The paused status of the query.
     */
    data class FetchFailure(
        val error: Throwable,
        val errorUpdatedAt: Long,
        val paused: QueryFetchStatus.Paused? = null
    ) : QueryAction<Nothing>

    /**
     * Invalidates the query.
     */
    data object Invalidate : QueryAction<Nothing>

    /**
     * Forces the query to update the data.
     *
     * @param data The data to be updated.
     * @param dataUpdatedAt The timestamp when the data was updated.
     */
    data class ForceUpdate<T>(
        val data: T,
        val dataUpdatedAt: Long
    ) : QueryAction<T>
}

typealias QueryReducer<T> = (QueryState<T>, QueryAction<T>) -> QueryState<T>
typealias QueryDispatch<T> = (QueryAction<T>) -> Unit

/**
 * Creates a [QueryReducer] function.
 */
fun <T> createQueryReducer(): QueryReducer<T> = { state, action ->
    when (action) {
        is QueryAction.Fetching -> {
            state.copy(
                fetchStatus = QueryFetchStatus.Fetching,
                isInvalidated = action.isInvalidated ?: state.isInvalidated
            )
        }

        is QueryAction.FetchSuccess -> {
            state.copy(
                reply = Reply(action.data),
                replyUpdatedAt = action.dataUpdatedAt,
                error = null,
                errorUpdatedAt = action.dataUpdatedAt,
                staleAt = action.dataStaleAt,
                status = QueryStatus.Success,
                fetchStatus = QueryFetchStatus.Idle,
                isInvalidated = false,
                isPlaceholderData = false
            )
        }

        is QueryAction.FetchFailure -> {
            state.copy(
                error = action.error,
                errorUpdatedAt = action.errorUpdatedAt,
                status = QueryStatus.Failure,
                fetchStatus = action.paused ?: QueryFetchStatus.Idle
            )
        }

        is QueryAction.Invalidate -> {
            state.copy(
                isInvalidated = true
            )
        }

        is QueryAction.ForceUpdate -> {
            state.copy(
                reply = Reply(action.data),
                replyUpdatedAt = action.dataUpdatedAt
            )
        }
    }
}
