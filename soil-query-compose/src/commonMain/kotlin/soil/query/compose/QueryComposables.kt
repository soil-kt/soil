// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import soil.query.QueryClient
import soil.query.QueryKey
import kotlin.jvm.JvmName

/**
 * Provides a conditional [rememberQuery].
 *
 * Calls [rememberQuery] only if [keyFactory] returns a [QueryKey] from [value].
 *
 * @see rememberQuery
 */
@Composable
fun <T, V> rememberQueryIf(
    value: V,
    keyFactory: (value: V) -> QueryKey<T>?,
    config: QueryConfig = QueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): QueryObject<T>? {
    val key = remember(value) { keyFactory(value) } ?: return null
    return rememberQuery(key, config, client)
}

/**
 * Provides a conditional [rememberQuery].
 *
 * Calls [rememberQuery] only if [keyFactory] returns a [QueryKey] from [value].
 *
 * @see rememberQuery
 */
@Composable
fun <T, U, V> rememberQueryIf(
    value: V,
    keyFactory: (value: V) -> QueryKey<T>?,
    select: (T) -> U,
    config: QueryConfig = QueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): QueryObject<U>? {
    val key = remember(value) { keyFactory(value) } ?: return null
    return rememberQuery(key, select, config, client)
}

/**
 * Provides a conditional [rememberQuery].
 *
 * Calls [rememberQuery] only if [keyPairFactory] returns a [Pair] of [QueryKey]s from [value].
 *
 * @see rememberQuery
 */
@JvmName("rememberQueryIfWithPair")
@Composable
fun <T1, T2, R, V> rememberQueryIf(
    value: V,
    keyPairFactory: (value: V) -> Pair<QueryKey<T1>, QueryKey<T2>>?,
    transform: (T1, T2) -> R,
    config: QueryConfig = QueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): QueryObject<R>? {
    val keyPair = remember(value) { keyPairFactory(value) } ?: return null
    return rememberQuery(keyPair.first, keyPair.second, transform, config, client)
}

/**
 * Provides a conditional [rememberQuery].
 *
 * Calls [rememberQuery] only if [keyTripleFactory] returns a [Triple] of [QueryKey]s from [value].
 *
 * @see rememberQuery
 */
@JvmName("rememberQueryIfWithTriple")
@Composable
fun <T1, T2, T3, R, V> rememberQueryIf(
    value: V,
    keyTripleFactory: (value: V) -> Triple<QueryKey<T1>, QueryKey<T2>, QueryKey<T3>>?,
    transform: (T1, T2, T3) -> R,
    config: QueryConfig = QueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): QueryObject<R>? {
    val keyTriple = remember(value) { keyTripleFactory(value) } ?: return null
    return rememberQuery(keyTriple.first, keyTriple.second, keyTriple.third, transform, config, client)
}

/**
 * Provides a conditional [rememberQuery].
 *
 * Calls [rememberQuery] only if [keyListFactory] returns a [List] of [QueryKey]s from [value].
 *
 * @see rememberQuery
 */
@JvmName("rememberQueryIfWithList")
@Composable
fun <T, R, V> rememberQueryIf(
    value: V,
    keyListFactory: (value: V) -> List<QueryKey<T>>?,
    transform: (List<T>) -> R,
    config: QueryConfig = QueryConfig.Default,
    client: QueryClient = LocalQueryClient.current
): QueryObject<R>? {
    val keys = remember(value) { keyListFactory(value) } ?: return null
    return rememberQuery(keys, transform, config, client)
}
