// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply
import soil.query.core.epoch

/**
 * State for managing the execution result of [Query].
 */
data class QueryState<T> internal constructor(
    override val reply: Reply<T> = Reply.None,
    override val replyUpdatedAt: Long = 0,
    override val error: Throwable? = null,
    override val errorUpdatedAt: Long = 0,
    override val staleAt: Long = 0,
    override val status: QueryStatus = QueryStatus.Pending,
    override val fetchStatus: QueryFetchStatus = QueryFetchStatus.Idle,
    override val isInvalidated: Boolean = false
) : QueryModel<T> {

    /**
     * Workaround:
     * The following warning appeared when updating the [reply] property within [SwrCache.setQueryData],
     * so I replaced the update process with a method that includes type information.
     * ref. https://youtrack.jetbrains.com/issue/KT-49404
     */
    internal fun patch(
        data: T,
        dataUpdatedAt: Long = epoch()
    ): QueryState<T> = copy(
        reply = Reply(data),
        replyUpdatedAt = dataUpdatedAt
    )

    companion object {

        /**
         * Creates a new [QueryState] with the [QueryStatus.Pending] status.
         */
        fun <T> initial(): QueryState<T> {
            return QueryState()
        }

        /**
         * Creates a new [QueryState] with the [QueryStatus.Success] status.
         *
         * @param data The data to be stored in the state.
         * @param dataUpdatedAt The timestamp when the data was updated. Default is the current epoch.
         * @param dataStaleAt The timestamp after which data is considered stale. Default is the same as [dataUpdatedAt].
         */
        fun <T> success(
            data: T,
            dataUpdatedAt: Long = epoch(),
            dataStaleAt: Long = dataUpdatedAt
        ): QueryState<T> {
            return QueryState(
                reply = Reply(data),
                replyUpdatedAt = dataUpdatedAt,
                staleAt = dataStaleAt,
                status = QueryStatus.Success
            )
        }

        /**
         * Creates a new [QueryState] with the [QueryStatus.Failure] status.
         *
         * @param error The error that occurred.
         * @param errorUpdatedAt The timestamp when the error occurred. Default is the current epoch.
         */
        fun <T> failure(
            error: Throwable,
            errorUpdatedAt: Long = epoch()
        ): QueryState<T> {
            return QueryState(
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                status = QueryStatus.Failure
            )
        }

        /**
         * Creates a new [QueryState] with the [QueryStatus.Failure] status.
         *
         * @param error The error that occurred.
         * @param errorUpdatedAt The timestamp when the error occurred. Default is the current epoch.
         * @param data The data to be stored in the state.
         * @param dataUpdatedAt The timestamp when the data was updated. Default is the current epoch.
         * @param dataStaleAt The timestamp after which data is considered stale. Default is the same as [dataUpdatedAt].
         */
        fun <T> failure(
            error: Throwable,
            errorUpdatedAt: Long = epoch(),
            data: T,
            dataUpdatedAt: Long = epoch(),
            dataStaleAt: Long = dataUpdatedAt
        ): QueryState<T> {
            return QueryState(
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                status = QueryStatus.Failure,
                reply = Reply(data),
                replyUpdatedAt = dataUpdatedAt,
                staleAt = dataStaleAt
            )
        }
    }
}
