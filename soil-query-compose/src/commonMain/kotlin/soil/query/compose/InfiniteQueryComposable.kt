// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.QueryChunks
import soil.query.QueryClient
import soil.query.QueryState
import soil.query.QueryStatus

@Composable
fun <T, S> rememberInfiniteQuery(
    key: InfiniteQueryKey<T, S>,
    client: QueryClient = LocalSwrClient.current
): InfiniteQueryObject<QueryChunks<T, S>, S> {
    val query = remember(key) { client.getInfiniteQuery(key) }
    val state by query.state.collectAsState()
    LaunchedEffect(query) {
        query.start(this)
    }
    return remember(query, state) {
        state.toInfiniteObject(query = query, select = { it })
    }
}

@Composable
fun <T, S, U> rememberInfiniteQuery(
    key: InfiniteQueryKey<T, S>,
    select: (chunks: QueryChunks<T, S>) -> U,
    client: QueryClient = LocalSwrClient.current
): InfiniteQueryObject<U, S> {
    val query = remember(key) { client.getInfiniteQuery(key) }
    val state by query.state.collectAsState()
    LaunchedEffect(query) {
        query.start(this)
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
