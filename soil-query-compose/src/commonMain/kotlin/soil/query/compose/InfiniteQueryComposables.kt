// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.query.InfiniteQueryKey
import soil.query.QueryChunks
import soil.query.QueryClient

/**
 * Provides a conditional [rememberInfiniteQuery].
 *
 * Calls [rememberInfiniteQuery] only if [keyFactory] returns a [InfiniteQueryKey] from [value].
 *
 * @see rememberInfiniteQuery
 */
@Composable
fun <T, S, V> rememberInfiniteQueryIf(
    value: V,
    keyFactory: (value: V) -> InfiniteQueryKey<T, S>?,
    config: InfiniteQueryConfig = InfiniteQueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): InfiniteQueryObject<QueryChunks<T, S>, S>? {
    val key = remember(value) { keyFactory(value) } ?: return null
    return rememberInfiniteQuery(key, config, client)
}

/**
 * Provides a conditional [rememberInfiniteQuery].
 *
 * Calls [rememberInfiniteQuery] only if [keyFactory] returns a [InfiniteQueryKey] from [value].
 *
 * @see rememberInfiniteQuery
 */
@Composable
fun <T, S, U, V> rememberInfiniteQueryIf(
    value: V,
    keyFactory: (value: V) -> InfiniteQueryKey<T, S>?,
    select: (chunks: QueryChunks<T, S>) -> U,
    config: InfiniteQueryConfig = InfiniteQueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): InfiniteQueryObject<U, S>? {
    val key = remember(value) { keyFactory(value) } ?: return null
    return rememberInfiniteQuery(key, select, config, client)
}
