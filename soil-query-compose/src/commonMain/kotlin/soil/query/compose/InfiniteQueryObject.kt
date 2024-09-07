// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import soil.query.QueryFetchStatus
import soil.query.QueryModel
import soil.query.QueryStatus
import soil.query.core.Reply
import soil.query.core.getOrNull
import soil.query.core.getOrThrow

/**
 * A InfiniteQueryObject represents [QueryModel]s interface for infinite fetching data using a retrieval method known as "infinite scroll."
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 */
@Stable
sealed interface InfiniteQueryObject<out T, S> : QueryModel<T> {

    /**
     * The return value from the data source. (Backward compatibility with QueryModel)
     */
    val data: T?

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
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val staleAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val refresh: suspend () -> Unit,
    override val loadMore: suspend (param: S) -> Unit,
    override val loadMoreParam: S?
) : InfiniteQueryObject<T, S> {
    override val status: QueryStatus = QueryStatus.Pending
    override val data: T? get() = reply.getOrNull()
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
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable,
    override val errorUpdatedAt: Long,
    override val staleAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val refresh: suspend () -> Unit,
    override val loadMore: suspend (param: S) -> Unit,
    override val loadMoreParam: S?
) : InfiniteQueryObject<T, S> {
    override val status: QueryStatus = QueryStatus.Failure
    override val data: T? get() = reply.getOrNull()
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
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable?,
    override val errorUpdatedAt: Long,
    override val staleAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val refresh: suspend () -> Unit,
    override val loadMore: suspend (param: S) -> Unit,
    override val loadMoreParam: S?
) : InfiniteQueryObject<T, S> {
    override val status: QueryStatus = QueryStatus.Success
    override val data: T get() = reply.getOrThrow()
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
    override val reply: Reply<T>,
    override val replyUpdatedAt: Long,
    override val error: Throwable,
    override val errorUpdatedAt: Long,
    override val staleAt: Long,
    override val fetchStatus: QueryFetchStatus,
    override val isInvalidated: Boolean,
    override val refresh: suspend () -> Unit,
    override val loadMore: suspend (param: S) -> Unit,
    override val loadMoreParam: S?
) : InfiniteQueryObject<T, S> {
    override val status: QueryStatus = QueryStatus.Failure
    override val data: T get() = reply.getOrThrow()
}
