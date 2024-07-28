// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.InfiniteQueryKey
import soil.query.MutationClient
import soil.query.MutationKey
import soil.query.QueryClient
import soil.query.QueryKey
import soil.query.ResumeQueriesFilter
import soil.query.SwrClient

typealias QueriesErrorReset = () -> Unit

/**
 * Remember a [QueriesErrorReset] to resume all queries with [filter] matched.
 *
 * @param filter The filter to match queries.
 * @param client The [SwrClient] to resume queries. By default, it uses the [LocalSwrClient].
 * @return A [QueriesErrorReset] to resume queries.
 */
@Composable
fun rememberQueriesErrorReset(
    filter: ResumeQueriesFilter = remember { ResumeQueriesFilter(predicate = { it.isFailure }) },
    client: SwrClient = LocalSwrClient.current
): QueriesErrorReset {
    val reset = remember<() -> Unit>(client) {
        { client.perform { resumeQueries(filter) } }
    }
    return reset
}

/**
 * Keep the query alive.
 *
 * Normally, a query stays active only when there are one or more references to it.
 * This function is useful when you want to keep the query active for some reason even if it's not directly needed.
 * For example, it can prevent data for a related query from becoming inactive, moving out of cache over time, such as when transitioning to a previous screen.
 *
 * @param key The [QueryKey] to keep alive.
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalSwrClient].
 */
@Composable
fun KeepAlive(
    key: QueryKey<*>,
    client: QueryClient = LocalSwrClient.current
) {
    val scope = rememberCoroutineScope()
    remember(key) { client.getQuery(key).also { it.launchIn(scope) } }
}

/**
 * Keep the infinite query alive.
 *
 * @param key The [InfiniteQueryKey] to keep alive.
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalSwrClient].
 *
 * @see KeepAlive
 */
@Composable
fun KeepAlive(
    key: InfiniteQueryKey<*, *>,
    client: QueryClient = LocalSwrClient.current
) {
    val scope = rememberCoroutineScope()
    remember(key) { client.getInfiniteQuery(key).also { it.launchIn(scope) } }
}

/**
 * Keep the mutation alive.
 *
 * @param key The [MutationKey] to keep alive.
 * @param client The [MutationClient] to resolve [key]. By default, it uses the [LocalSwrClient].
 *
 * @see KeepAlive
 */
@Composable
fun KeepAlive(
    key: MutationKey<*, *>,
    client: MutationClient = LocalSwrClient.current
) {
    val scope = rememberCoroutineScope()
    remember(key) { client.getMutation(key).also { it.launchIn(scope) } }
}
