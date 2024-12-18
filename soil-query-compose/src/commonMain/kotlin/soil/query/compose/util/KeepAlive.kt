// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import soil.query.InfiniteQueryKey
import soil.query.MutationClient
import soil.query.MutationKey
import soil.query.QueryClient
import soil.query.QueryKey
import soil.query.compose.LocalMutationClient
import soil.query.compose.LocalQueryClient

/**
 * Keep the query alive.
 *
 * Normally, a query stays active only when there are one or more references to it.
 * This function is useful when you want to keep the query active for some reason even if it's not directly needed.
 * For example, it can prevent data for a related query from becoming inactive, moving out of cache over time, such as when transitioning to a previous screen.
 *
 * @param key The [QueryKey] to keep alive.
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalQueryClient].
 */
@Composable
fun KeepAlive(
    key: QueryKey<*>,
    client: QueryClient = LocalQueryClient.current
) {
    @Suppress("UNUSED_VARIABLE") val q = remember(key) { client.getQuery(key) }
}

/**
 * Keep the infinite query alive.
 *
 * @param key The [InfiniteQueryKey] to keep alive.
 * @param client The [QueryClient] to resolve [key]. By default, it uses the [LocalQueryClient].
 *
 * @see KeepAlive
 */
@Composable
fun KeepAlive(
    key: InfiniteQueryKey<*, *>,
    client: QueryClient = LocalQueryClient.current
) {
    @Suppress("UNUSED_VARIABLE") val q = remember(key) { client.getInfiniteQuery(key) }
}

/**
 * Keep the mutation alive.
 *
 * @param key The [MutationKey] to keep alive.
 * @param client The [MutationClient] to resolve [key]. By default, it uses the [LocalMutationClient].
 *
 * @see KeepAlive
 */
@Composable
fun KeepAlive(
    key: MutationKey<*, *>,
    client: MutationClient = LocalMutationClient.current
) {
    @Suppress("UNUSED_VARIABLE") val q = remember(key) { client.getMutation(key) }
}
