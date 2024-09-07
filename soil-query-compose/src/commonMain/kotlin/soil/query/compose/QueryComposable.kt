// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.QueryClient
import soil.query.QueryKey

/**
 * Remember a [QueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param key The [QueryKey] for managing [query][soil.query.Query].
 * @param config The configuration for the query. By default, it uses the [QueryConfig.Default].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalQueryClient].
 * @return A [QueryObject] each the query state changed.
 */
@Composable
fun <T> rememberQuery(
    key: QueryKey<T>,
    config: QueryConfig = QueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): QueryObject<T> {
    val scope = rememberCoroutineScope()
    val query = remember(key) { client.getQuery(key, config.marker).also { it.launchIn(scope) } }
    return with(config.mapper) {
        config.strategy.collectAsState(query).toObject(query = query, select = { it })
    }
}

/**
 * Remember a [QueryObject] and subscribes to the query state of [key].
 *
 * @param T Type of data to retrieve.
 * @param U Type of selected data.
 * @param key The [QueryKey] for managing [query][soil.query.Query].
 * @param select A function to select data from [T].
 * @param config The configuration for the query. By default, it uses the [QueryConfig.Default].
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalQueryClient].
 * @return A [QueryObject] with selected data each the query state changed.
 */
@Composable
fun <T, U> rememberQuery(
    key: QueryKey<T>,
    select: (T) -> U,
    config: QueryConfig = QueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): QueryObject<U> {
    val scope = rememberCoroutineScope()
    val query = remember(key) { client.getQuery(key, config.marker).also { it.launchIn(scope) } }
    return with(config.mapper) {
        config.strategy.collectAsState(query).toObject(query = query, select = select)
    }
}
