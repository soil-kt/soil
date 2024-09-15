// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.InfiniteQueryKey
import soil.query.QueryChunks
import soil.query.QueryClient

/**
 * Remember a [InfiniteQueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @param key The [InfiniteQueryKey] for managing [query][soil.query.Query] associated with [id][soil.query.InfiniteQueryId].
 * @param config The configuration for the query. By default, it uses the [InfiniteQueryConfig.Default].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalQueryClient].
 * @return A [InfiniteQueryObject] each the query state changed.
 */
@Composable
fun <T, S> rememberInfiniteQuery(
    key: InfiniteQueryKey<T, S>,
    config: InfiniteQueryConfig = InfiniteQueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): InfiniteQueryObject<QueryChunks<T, S>, S> {
    val scope = rememberCoroutineScope()
    val query = remember(key.id) { client.getInfiniteQuery(key, config.marker).also { it.launchIn(scope) } }
    return with(config.mapper) {
        config.strategy.collectAsState(query).toObject(query = query, select = { it })
    }
}

/**
 * Remember a [InfiniteQueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @param key The [InfiniteQueryKey] for managing [query][soil.query.Query] associated with [id][soil.query.InfiniteQueryId].
 * @param select A function to select data from [QueryChunks].
 * @param config The configuration for the query. By default, it uses the [InfiniteQueryConfig.Default].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalQueryClient].
 * @return A [InfiniteQueryObject] with selected data each the query state changed.
 */
@Composable
fun <T, S, U> rememberInfiniteQuery(
    key: InfiniteQueryKey<T, S>,
    select: (chunks: QueryChunks<T, S>) -> U,
    config: InfiniteQueryConfig = InfiniteQueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): InfiniteQueryObject<U, S> {
    val scope = rememberCoroutineScope()
    val query = remember(key.id) { client.getInfiniteQuery(key, config.marker).also { it.launchIn(scope) } }
    return with(config.mapper) {
        config.strategy.collectAsState(query).toObject(query = query, select = select)
    }
}
