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
