// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.epoch

/**
 * State for managing the execution result of [Query].
 */
data class QueryState<out T> internal constructor(
    override val data: T? = null,
    override val dataUpdatedAt: Long = 0,
    override val dataStaleAt: Long = 0,
    override val error: Throwable? = null,
    override val errorUpdatedAt: Long = 0,
    override val status: QueryStatus = QueryStatus.Pending,
    override val fetchStatus: QueryFetchStatus = QueryFetchStatus.Idle,
    override val isInvalidated: Boolean = false,
    override val isPlaceholderData: Boolean = false
) : QueryModel<T> {
    companion object {

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
                data = data,
                dataUpdatedAt = dataUpdatedAt,
                dataStaleAt = dataStaleAt,
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
    }
}
