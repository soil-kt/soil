// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.TimeBasedCache
import soil.query.core.UniqueId
import soil.query.core.epoch
import kotlin.time.Duration

typealias QueryCache = TimeBasedCache<UniqueId, QueryState<*>>

/**
 * Creates a new query cache.
 */
fun QueryCache(capacity: Int = 50): QueryCache {
    return TimeBasedCache(capacity)
}

/**
 * Builder for creating a query cache.
 */
interface QueryCacheBuilder {

    /**
     * Puts the query data into the cache.
     *
     * @param id Unique identifier for the query.
     * @param data Data to store.
     * @param dataUpdatedAt Timestamp when the data was updated. Default is the current epoch time.
     * @param dataStaleAt The timestamp after which data is considered stale. Default is the same as [dataUpdatedAt]
     * @param ttl Time to live for the data. Default is [Duration.INFINITE]
     */
    fun <T> put(
        id: QueryId<T>,
        data: T,
        dataUpdatedAt: Long = epoch(),
        dataStaleAt: Long = dataUpdatedAt,
        ttl: Duration = Duration.INFINITE
    )

    /**
     * Puts the infinite-query data into the cache.
     *
     * @param id Unique identifier for the infinite query.
     * @param data Data to store.
     * @param dataUpdatedAt Timestamp when the data was updated. Default is the current epoch time.
     * @param dataStaleAt The timestamp after which data is considered stale. Default is the same as [dataUpdatedAt]
     * @param ttl Time to live for the data. Default is [Duration.INFINITE]
     */
    fun <T, S> put(
        id: InfiniteQueryId<T, S>,
        data: QueryChunks<T, S>,
        dataUpdatedAt: Long = epoch(),
        dataStaleAt: Long = dataUpdatedAt,
        ttl: Duration = Duration.INFINITE
    )
}

/**
 * Creates a new query cache with the specified [capacity] and applies the [block] to the builder.
 *
 * ```kotlin
 * val cache = QueryCacheBuilder {
 *    put(GetUserKey.Id(userId), user)
 *    ..
 * }
 * ```
 */
@Suppress("FunctionName")
fun QueryCacheBuilder(capacity: Int = 50, block: QueryCacheBuilder.() -> Unit): QueryCache {
    return DefaultQueryCacheBuilder(capacity).apply(block).build()
}

internal class DefaultQueryCacheBuilder(capacity: Int) : QueryCacheBuilder {
    private val cache = QueryCache(capacity)

    override fun <T> put(
        id: QueryId<T>,
        data: T,
        dataUpdatedAt: Long,
        dataStaleAt: Long,
        ttl: Duration
    ) = cache.set(id, QueryState.success(data, dataUpdatedAt, dataStaleAt), ttl)

    override fun <T, S> put(
        id: InfiniteQueryId<T, S>,
        data: QueryChunks<T, S>,
        dataUpdatedAt: Long,
        dataStaleAt: Long,
        ttl: Duration
    ) = cache.set(id, QueryState.success(data, dataUpdatedAt, dataStaleAt), ttl)

    fun build(): QueryCache {
        return cache
    }
}
