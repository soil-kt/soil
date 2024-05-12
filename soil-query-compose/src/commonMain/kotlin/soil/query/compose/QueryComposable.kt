// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import soil.query.QueryClient
import soil.query.QueryKey
import soil.query.QueryRef
import soil.query.QueryState
import soil.query.QueryStatus

/**
 * Remember a [QueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param key The [QueryKey] for managing [query][soil.query.Query].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalSwrClient].
 * @return A [QueryObject] each the query state changed.
 */
@Composable
fun <T> rememberQuery(
    key: QueryKey<T>,
    client: QueryClient = LocalSwrClient.current
): QueryObject<T> {
    val query = remember(key) { client.getQuery(key) }
    val state by query.state.collectAsState()
    LaunchedEffect(query) {
        query.start(this)
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
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalSwrClient].
 * @return A [QueryObject] with selected data each the query state changed.
 */
@Composable
fun <T, U> rememberQuery(
    key: QueryKey<T>,
    select: (T) -> U,
    client: QueryClient = LocalSwrClient.current
): QueryObject<U> {
    val query = remember(key) { client.getQuery(key) }
    val state by query.state.collectAsState()
    LaunchedEffect(query) {
        query.start(this)
    }
    return remember(query, state) {
        state.toObject(query = query, select = select)
    }
}

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
            data = select(data!!),
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
                data = select(data!!),
                dataUpdatedAt = dataUpdatedAt,
                dataStaleAt = dataStaleAt,
                error = error!!,
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
                error = error!!,
                errorUpdatedAt = errorUpdatedAt,
                fetchStatus = fetchStatus,
                isInvalidated = isInvalidated,
                isPlaceholderData = isPlaceholderData,
                refresh = query::invalidate
            )
        }
    }
}
