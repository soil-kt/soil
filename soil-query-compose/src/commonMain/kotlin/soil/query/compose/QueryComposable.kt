// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.QueryClient
import soil.query.QueryKey
import soil.query.compose.internal.newCombinedQuery
import soil.query.compose.internal.newQuery

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
    val query = remember(key.id) { newQuery(key, config, client, scope) }
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
    val query = remember(key.id) { newQuery(key, config, client, scope) }
    return with(config.mapper) {
        config.strategy.collectAsState(query).toObject(query = query, select = select)
    }
}

/**
 * Remember a [QueryObject] and subscribes to the query state of [key1] and [key2].
 *
 * @param T1 Type of data to retrieve from [key1].
 * @param T2 Type of data to retrieve from [key2].
 * @param R Type of data to transform.
 * @param key1 The [QueryKey] for managing [query][soil.query.Query].
 * @param key2 The [QueryKey] for managing [query][soil.query.Query].
 * @param transform A function to transform [T1] and [T2] into [R].
 * @param config The configuration for the query. By default, it uses the [QueryConfig.Default].
 * @param client The [QueryClient] to resolve [key1] and [key2]. By default, it uses the [LocalQueryClient].
 * @return A [QueryObject] with transformed data each the query state changed.
 */
@Composable
fun <T1, T2, R> rememberQuery(
    key1: QueryKey<T1>,
    key2: QueryKey<T2>,
    transform: (T1, T2) -> R,
    config: QueryConfig = QueryConfig.Default,
    client: QueryClient = LocalQueryClient.current,
): QueryObject<R> {
    val scope = rememberCoroutineScope()
    val query = remember(key1.id, key2.id) {
        newCombinedQuery(key1, key2, transform, config, client, scope)
    }
    return with(config.mapper) {
        config.strategy.collectAsState(query).toObject(query = query, select = { it })
    }
}

/**
 * Remember a [QueryObject] and subscribes to the query state of [key1], [key2], and [key3].
 *
 * @param T1 Type of data to retrieve from [key1].
 * @param T2 Type of data to retrieve from [key2].
 * @param T3 Type of data to retrieve from [key3].
 * @param R Type of data to transform.
 * @param key1 The [QueryKey] for managing [query][soil.query.Query].
 * @param key2 The [QueryKey] for managing [query][soil.query.Query].
 * @param key3 The [QueryKey] for managing [query][soil.query.Query].
 * @param transform A function to transform [T1], [T2], and [T3] into [R].
 * @param config The configuration for the query. By default, it uses the [QueryConfig.Default].
 * @param client The [QueryClient] to resolve [key1], [key2], and [key3]. By default, it uses the [LocalQueryClient].
 * @return A [QueryObject] with transformed data each the query state changed.
 */
@Composable
fun <T1, T2, T3, R> rememberQuery(
    key1: QueryKey<T1>,
    key2: QueryKey<T2>,
    key3: QueryKey<T3>,
    transform: (T1, T2, T3) -> R,
    config: QueryConfig = QueryConfig.Default,
    client: QueryClient = LocalQueryClient.current,
): QueryObject<R> {
    val scope = rememberCoroutineScope()
    val query = remember(key1.id, key2.id, key3.id) {
        newCombinedQuery(key1, key2, key3, transform, config, client, scope)
    }
    return with(config.mapper) {
        config.strategy.collectAsState(query).toObject(query = query, select = { it })
    }
}

/**
 * Remember a [QueryObject] and subscribes to the query state of [keys].
 *
 * @param T Type of data to retrieve.
 * @param R Type of data to transform.
 * @param keys The list of [QueryKey] for managing [query][soil.query.Query].
 * @param transform A function to transform [T] into [R].
 * @param config The configuration for the query. By default, it uses the [QueryConfig.Default].
 * @param client The [QueryClient] to resolve [keys]. By default, it uses the [LocalQueryClient].
 * @return A [QueryObject] with transformed data each the query state changed.
 */
@Composable
fun <T, R> rememberQuery(
    keys: List<QueryKey<T>>,
    transform: (List<T>) -> R,
    config: QueryConfig = QueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): QueryObject<R> {
    val scope = rememberCoroutineScope()
    val query = remember(*keys.map { it.id }.toTypedArray()) {
        newCombinedQuery(keys, transform, config, client, scope)
    }
    return with(config.mapper) {
        config.strategy.collectAsState(query).toObject(query = query, select = { it })
    }
}
