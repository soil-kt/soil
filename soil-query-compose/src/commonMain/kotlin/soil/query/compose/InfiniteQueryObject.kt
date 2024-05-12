// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import soil.query.QueryFetchStatus
import soil.query.QueryModel
import soil.query.QueryStatus

/**
 * A InfiniteQueryObject represents [QueryModel]s interface for infinite fetching data using a retrieval method known as "infinite scroll."
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 */
@Stable
sealed interface InfiniteQueryObject<out T, S> : QueryModel<T> {

    /**
     * Refreshes the data.
     */
    val refresh: suspend () -> Unit

    /**
     * Fetches data for the [InfiniteQueryKey][soil.query.InfiniteQueryKey] using the [parameter][loadMoreParam].
     */
    val loadMore: suspend (param: S) -> Unit

    /**
     * The parameter for next fetching. If null, it means there is no more data to fetch.
     */
    val loadMoreParam: S?
}

/**
 * A InfiniteQueryLoadingObject represents the initial loading state of the [InfiniteQueryObject].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @constructor Creates a [InfiniteQueryLoadingObject].
 */
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

/**
 * A InfiniteQueryLoadingErrorObject represents the initial loading error state of the [InfiniteQueryObject].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @constructor Creates a [InfiniteQueryLoadingErrorObject].
 */
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

/**
 * A InfiniteQuerySuccessObject represents the successful state of the [InfiniteQueryObject].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @constructor Creates a [InfiniteQuerySuccessObject].
 */
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

/**
 * A InfiniteQueryRefreshErrorObject represents the refresh error state of the [InfiniteQueryObject].
 *
 * This state is used when the data is successfully retrieved once, but an error occurs during the refresh or additional fetching.
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @constructor Creates a [InfiniteQueryRefreshErrorObject].
 */
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
