// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.QueryClient
import soil.query.QueryKey
import soil.query.QueryRef
import soil.query.QueryState
import soil.query.QueryStatus
import soil.query.invalidate
import soil.query.resume

/**
 * Remember a [QueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param key The [QueryKey] for managing [query][soil.query.Query].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalQueryClient].
 * @return A [QueryObject] each the query state changed.
 */
@Composable
fun <T> rememberQuery(
    key: QueryKey<T>,
    client: QueryClient = LocalQueryClient.current
): QueryObject<T> {
    val scope = rememberCoroutineScope()
    val query = remember(key) { client.getQuery(key).also { it.launchIn(scope) } }
    val state by query.state.collectAsState()
    LaunchedEffect(query) {
        query.resume()
    }
    return remember(query, state) {
        state.toObject(query = query, select = { it })
    }
}

/**
 * Remember a [QueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param U Type of selected data.
 * @param key The [QueryKey] for managing [query][soil.query.Query].
 * @param select A function to select data from [T].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalQueryClient].
 * @return A [QueryObject] with selected data each the query state changed.
 */
@Composable
fun <T, U> rememberQuery(
    key: QueryKey<T>,
    select: (T) -> U,
    client: QueryClient = LocalQueryClient.current
): QueryObject<U> {
    val scope = rememberCoroutineScope()
    val query = remember(key) { client.getQuery(key).also { it.launchIn(scope) } }
    val state by query.state.collectAsState()
    LaunchedEffect(query) {
        query.resume()
    }
    return remember(query, state) {
        state.toObject(query = query, select = select)
    }
}

@Suppress("UNCHECKED_CAST")
private fun <T, U> QueryState<T>.toObject(
    query: QueryRef<T>,
    select: (T) -> U
): QueryObject<U> {
    return when (status) {
        QueryStatus.Pending -> QueryLoadingObject(
            data = data?.let(select),
            dataUpdatedAt = dataUpdatedAt,
            dataStaleAt = dataStaleAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            fetchStatus = fetchStatus,
            isInvalidated = isInvalidated,
            isPlaceholderData = isPlaceholderData,
            refresh = query::invalidate
        )

        QueryStatus.Success -> QuerySuccessObject(
            data = select(data as T),
            dataUpdatedAt = dataUpdatedAt,
            dataStaleAt = dataStaleAt,
            error = error,
            errorUpdatedAt = errorUpdatedAt,
            fetchStatus = fetchStatus,
            isInvalidated = isInvalidated,
            isPlaceholderData = isPlaceholderData,
            refresh = query::invalidate
        )

        QueryStatus.Failure -> if (dataUpdatedAt > 0) {
            QueryRefreshErrorObject(
                data = select(data as T),
                dataUpdatedAt = dataUpdatedAt,
                dataStaleAt = dataStaleAt,
                error = error as Throwable,
                errorUpdatedAt = errorUpdatedAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                isPlaceholderData = isPlaceholderData,
                refresh = query::invalidate
            )
        } else {
            QueryLoadingErrorObject(
                data = data?.let(select),
                dataUpdatedAt = dataUpdatedAt,
                dataStaleAt = dataStaleAt,
                error = error as Throwable,
                errorUpdatedAt = errorUpdatedAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                isPlaceholderData = isPlaceholderData,
                refresh = query::invalidate
            )
        }
    }
}
