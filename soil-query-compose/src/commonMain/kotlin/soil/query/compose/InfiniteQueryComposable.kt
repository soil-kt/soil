// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.QueryChunks
import soil.query.QueryClient
import soil.query.QueryState
import soil.query.QueryStatus
import soil.query.core.getOrThrow
import soil.query.core.isNone
import soil.query.core.map
import soil.query.invalidate
import soil.query.loadMore

/**
 * Remember a [InfiniteQueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @param key The [InfiniteQueryKey] for managing [query][soil.query.Query] associated with [id][soil.query.InfiniteQueryId].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalQueryClient].
 * @return A [InfiniteQueryObject] each the query state changed.
 */
@Composable
fun <T, S> rememberInfiniteQuery(
    key: InfiniteQueryKey<T, S>,
    strategy: QueryCachingStrategy = QueryCachingStrategy,
    client: QueryClient = LocalQueryClient.current
): InfiniteQueryObject<QueryChunks<T, S>, S> {
    val scope = rememberCoroutineScope()
    val query = remember(key) { client.getInfiniteQuery(key).also { it.launchIn(scope) } }
    return strategy.collectAsState(query).toInfiniteObject(query = query, select = { it })
}

/**
 * Remember a [InfiniteQueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @param key The [InfiniteQueryKey] for managing [query][soil.query.Query] associated with [id][soil.query.InfiniteQueryId].
 * @param select A function to select data from [QueryChunks].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalQueryClient].
 * @return A [InfiniteQueryObject] with selected data each the query state changed.
 */
@Composable
fun <T, S, U> rememberInfiniteQuery(
    key: InfiniteQueryKey<T, S>,
    select: (chunks: QueryChunks<T, S>) -> U,
    strategy: QueryCachingStrategy = QueryCachingStrategy,
    client: QueryClient = LocalQueryClient.current
): InfiniteQueryObject<U, S> {
    val scope = rememberCoroutineScope()
    val query = remember(key) { client.getInfiniteQuery(key).also { it.launchIn(scope) } }
    return strategy.collectAsState(query).toInfiniteObject(query = query, select = select)
}

private fun <T, S, U> QueryState<QueryChunks<T, S>>.toInfiniteObject(
    query: InfiniteQueryRef<T, S>,
    select: (chunks: QueryChunks<T, S>) -> U
): InfiniteQueryObject<U, S> {
    return when (status) {
        QueryStatus.Pending -> InfiniteQueryLoadingObject(
            reply = reply.map(select),
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            staleAt = staleAt,
            fetchStatus = fetchStatus,
            isInvalidated = isInvalidated,
            refresh = query::invalidate,
            loadMore = query::loadMore,
            loadMoreParam = null
        )

        QueryStatus.Success -> InfiniteQuerySuccessObject(
            reply = reply.map(select),
            replyUpdatedAt = replyUpdatedAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            staleAt = staleAt,
            fetchStatus = fetchStatus,
            isInvalidated = isInvalidated,
            refresh = query::invalidate,
            loadMore = query::loadMore,
            loadMoreParam = query.key.loadMoreParam(reply.getOrThrow())
        )

        QueryStatus.Failure -> if (reply.isNone) {
            InfiniteQueryLoadingErrorObject(
                reply = reply.map(select),
                replyUpdatedAt = replyUpdatedAt,
                error = checkNotNull(error),
                errorUpdatedAt = errorUpdatedAt,
                staleAt = staleAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                refresh = query::invalidate,
                loadMore = query::loadMore,
                loadMoreParam = null
            )
        } else {
            InfiniteQueryRefreshErrorObject(
                reply = reply.map(select),
                replyUpdatedAt = replyUpdatedAt,
                error = checkNotNull(error),
                errorUpdatedAt = errorUpdatedAt,
                staleAt = staleAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                refresh = query::invalidate,
                loadMore = query::loadMore,
                loadMoreParam = query.key.loadMoreParam(reply.getOrThrow())
            )
        }
    }
}
