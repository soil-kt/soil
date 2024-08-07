// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.QueryChunks
import soil.query.QueryClient
import soil.query.QueryState
import soil.query.QueryStatus

/**
 * Remember a [InfiniteQueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @param key The [InfiniteQueryKey] for managing [query][soil.query.Query] associated with [id][soil.query.InfiniteQueryId].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalSwrClient].
 * @return A [InfiniteQueryObject] each the query state changed.
 */
@Composable
fun <T, S> rememberInfiniteQuery(
    key: InfiniteQueryKey<T, S>,
    client: QueryClient = LocalSwrClient.current
): InfiniteQueryObject<QueryChunks<T, S>, S> {
    val scope = rememberCoroutineScope()
    val query = remember(key) { client.getInfiniteQuery(key).also { it.launchIn(scope) } }
    val state by query.state.collectAsState()
    LaunchedEffect(query) {
        query.start()
    }
    return remember(query, state) {
        state.toInfiniteObject(query = query, select = { it })
    }
}

/**
 * Remember a [InfiniteQueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @param key The [InfiniteQueryKey] for managing [query][soil.query.Query] associated with [id][soil.query.InfiniteQueryId].
 * @param select A function to select data from [QueryChunks].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalSwrClient].
 * @return A [InfiniteQueryObject] with selected data each the query state changed.
 */
@Composable
fun <T, S, U> rememberInfiniteQuery(
    key: InfiniteQueryKey<T, S>,
    select: (chunks: QueryChunks<T, S>) -> U,
    client: QueryClient = LocalSwrClient.current
): InfiniteQueryObject<U, S> {
    val scope = rememberCoroutineScope()
    val query = remember(key) { client.getInfiniteQuery(key).also { it.launchIn(scope) } }
    val state by query.state.collectAsState()
    LaunchedEffect(query) {
        query.start()
    }
    return remember(query, state) {
        state.toInfiniteObject(query = query, select = select)
    }
}

private fun <T, S, U> QueryState<QueryChunks<T, S>>.toInfiniteObject(
    query: InfiniteQueryRef<T, S>,
    select: (chunks: QueryChunks<T, S>) -> U
): InfiniteQueryObject<U, S> {
    return when (status) {
        QueryStatus.Pending -> InfiniteQueryLoadingObject(
            data = data?.let(select),
            dataUpdatedAt = dataUpdatedAt,
            dataStaleAt = dataStaleAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            fetchStatus = fetchStatus,
            isInvalidated = isInvalidated,
            isPlaceholderData = isPlaceholderData,
            refresh = query::invalidate,
            loadMore = query::loadMore,
            loadMoreParam = null
        )

        QueryStatus.Success -> InfiniteQuerySuccessObject(
            data = select(data!!),
            dataUpdatedAt = dataUpdatedAt,
            dataStaleAt = dataStaleAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            fetchStatus = fetchStatus,
            isInvalidated = isInvalidated,
            isPlaceholderData = isPlaceholderData,
            refresh = query::invalidate,
            loadMore = query::loadMore,
            loadMoreParam = query.key.loadMoreParam(data!!)
        )

        QueryStatus.Failure -> if (dataUpdatedAt > 0) {
            InfiniteQueryRefreshErrorObject(
                data = select(data!!),
                dataUpdatedAt = dataUpdatedAt,
                dataStaleAt = dataStaleAt,
                error = error!!,
                errorUpdatedAt = errorUpdatedAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                isPlaceholderData = isPlaceholderData,
                refresh = query::invalidate,
                loadMore = query::loadMore,
                loadMoreParam = query.key.loadMoreParam(data!!)
            )
        } else {
            InfiniteQueryLoadingErrorObject(
                data = data?.let(select),
                dataUpdatedAt = dataUpdatedAt,
                dataStaleAt = dataStaleAt,
                error = error!!,
                errorUpdatedAt = errorUpdatedAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                isPlaceholderData = isPlaceholderData,
                refresh = query::invalidate,
                loadMore = query::loadMore,
                loadMoreParam = null
            )
        }
    }
}
