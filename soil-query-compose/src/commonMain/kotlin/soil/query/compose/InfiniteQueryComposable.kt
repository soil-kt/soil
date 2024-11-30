// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.QueryChunks
import soil.query.QueryClient
import soil.query.compose.internal.newInfiniteQuery

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
    val query = remember(key.id) { newInfiniteQuery(key, config, client, scope) }
    query.Effect()
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
    val query = remember(key.id) { newInfiniteQuery(key, config, client, scope) }
    query.Effect()
    return with(config.mapper) {
        config.strategy.collectAsState(query).toObject(query = query, select = select)
    }
}

@Suppress("NOTHING_TO_INLINE", "KotlinRedundantDiagnosticSuppress")
@Composable
private inline fun InfiniteQueryRef<*, *>.Effect() {
    // TODO: Switch to LifecycleResumeEffect
    //  Android, it works only with Compose UI 1.7.0-alpha05 or above.
    //  Therefore, we will postpone adding this code until a future release.
    LaunchedEffect(id) {
        join()
    }
}
