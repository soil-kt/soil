// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply
import soil.query.core.epoch
import kotlin.jvm.JvmInline

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

    /**
     * Returns a new [QueryState] with the items included in [keys] omitted from the current [QueryState].
     *
     * NOTE: This function is provided to optimize recomposition for Compose APIs.
     */
    fun omit(keys: Set<OmitKey>): QueryState<T> {
        if (keys.isEmpty()) return this
        return copy(
            replyUpdatedAt = if (keys.contains(OmitKey.replyUpdatedAt)) 0 else replyUpdatedAt,
            errorUpdatedAt = if (keys.contains(OmitKey.errorUpdatedAt)) 0 else errorUpdatedAt,
            staleAt = if (keys.contains(OmitKey.staleAt)) 0 else staleAt,
            fetchStatus = if (keys.contains(OmitKey.fetchStatus)) QueryFetchStatus.Idle else fetchStatus
        )
    }

    @JvmInline
    value class OmitKey(val name: String) {
        companion object {
            val replyUpdatedAt = OmitKey("replyUpdatedAt")
            val errorUpdatedAt = OmitKey("errorUpdatedAt")
            val staleAt = OmitKey("staleAt")
            val fetchStatus = OmitKey("fetchStatus")
        }
    }

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

        /**
         * Creates a new [QueryState] for Testing.
         *
         * NOTE: **This method is for testing purposes only.**
         */
        fun <T> test(
            reply: Reply<T> = Reply.None,
            replyUpdatedAt: Long = 0,
            error: Throwable? = null,
            errorUpdatedAt: Long = 0,
            staleAt: Long = 0,
            status: QueryStatus = QueryStatus.Pending,
            fetchStatus: QueryFetchStatus = QueryFetchStatus.Idle,
            isInvalidated: Boolean = false
        ): QueryState<T> {
            return QueryState(
                reply = reply,
                replyUpdatedAt = replyUpdatedAt,
                error = error,
                errorUpdatedAt = errorUpdatedAt,
                staleAt = staleAt,
                status = status,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated
            )
        }
    }
}
