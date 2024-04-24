// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import soil.query.QueryFetchStatus
import soil.query.QueryModel
import soil.query.QueryStatus

@Stable
sealed interface InfiniteQueryObject<out T, S> : QueryModel<T> {
    val refresh: suspend () -> Unit
    val loadMore: suspend (param: S) -> Unit
    val loadMoreParam: S?
}

@Immutable
data class InfiniteQueryLoadingObject<T, S>(
    override val data: T?,
    override val dataUpdatedAt: Long,
    override val dataStaleAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val isPlaceholderData: Boolean,
    override val refresh: suspend () -> Unit,
    override val loadMore: suspend (param: S) -> Unit,
    override val loadMoreParam: S?
) : InfiniteQueryObject<T, S> {
    override val status: QueryStatus = QueryStatus.Pending
}

@Immutable
data class InfiniteQueryLoadingErrorObject<T, S>(
    override val data: T?,
    override val dataUpdatedAt: Long,
    override val dataStaleAt: Long,
    override val error: Throwable,
    override val errorUpdatedAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val isPlaceholderData: Boolean,
    override val refresh: suspend () -> Unit,
    override val loadMore: suspend (param: S) -> Unit,
    override val loadMoreParam: S?
) : InfiniteQueryObject<T, S> {
    override val status: QueryStatus = QueryStatus.Failure
}

@Immutable
data class InfiniteQuerySuccessObject<T, S>(
    override val data: T,
    override val dataUpdatedAt: Long,
    override val dataStaleAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val isPlaceholderData: Boolean,
    override val refresh: suspend () -> Unit,
    override val loadMore: suspend (param: S) -> Unit,
    override val loadMoreParam: S?
) : InfiniteQueryObject<T, S> {
    override val status: QueryStatus = QueryStatus.Success
}

@Immutable
data class InfiniteQueryRefreshErrorObject<T, S>(
    override val data: T,
    override val dataUpdatedAt: Long,
    override val dataStaleAt: Long,
    override val error: Throwable,
    override val errorUpdatedAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val isPlaceholderData: Boolean,
    override val refresh: suspend () -> Unit,
    override val loadMore: suspend (param: S) -> Unit,
    override val loadMoreParam: S?
) : InfiniteQueryObject<T, S> {
    override val status: QueryStatus = QueryStatus.Failure
}
