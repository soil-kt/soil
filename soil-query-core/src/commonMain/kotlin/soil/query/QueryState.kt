// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

/**
 * State for managing the execution result of [Query].
 */
data class QueryState<out T>(
    override val data: T? = null,
    override val dataUpdatedAt: Long = 0,
    override val dataStaleAt: Long = 0,
    override val error: Throwable? = null,
    override val errorUpdatedAt: Long = 0,
    override val status: QueryStatus = QueryStatus.Pending,
    override val fetchStatus: QueryFetchStatus = QueryFetchStatus.Idle,
    override val isInvalidated: Boolean = false,
    override val isPlaceholderData: Boolean = false
) : QueryModel<T>
