// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import soil.query.QueryFetchStatus
import soil.query.QueryModel
import soil.query.QueryStatus


@Stable
sealed interface QueryObject<out T> : QueryModel<T> {
    val refresh: suspend () -> Unit
}

@Immutable
data class QueryLoadingObject<T>(
    override val data: T?,
    override val dataUpdatedAt: Long,
    override val dataStaleAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val isPlaceholderData: Boolean,
    override val refresh: suspend () -> Unit
) : QueryObject<T> {
    override val status: QueryStatus = QueryStatus.Pending
}

@Immutable
data class QueryLoadingErrorObject<T>(
    override val data: T?,
    override val dataUpdatedAt: Long,
    override val dataStaleAt: Long,
    override val error: Throwable,
    override val errorUpdatedAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val isPlaceholderData: Boolean,
    override val refresh: suspend () -> Unit
) : QueryObject<T> {
    override val status: QueryStatus = QueryStatus.Failure
}

@Immutable
data class QuerySuccessObject<T>(
    override val data: T,
    override val dataUpdatedAt: Long,
    override val dataStaleAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val isPlaceholderData: Boolean,
    override val refresh: suspend () -> Unit
) : QueryObject<T> {
    override val status: QueryStatus = QueryStatus.Success
}

@Immutable
data class QueryRefreshErrorObject<T>(
    override val data: T,
    override val dataUpdatedAt: Long,
    override val dataStaleAt: Long,
    override val error: Throwable,
    override val errorUpdatedAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val isPlaceholderData: Boolean,
    override val refresh: suspend () -> Unit
) : QueryObject<T> {
    override val status: QueryStatus = QueryStatus.Failure
}
