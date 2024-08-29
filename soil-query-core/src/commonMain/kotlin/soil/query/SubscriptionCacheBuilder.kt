// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.TimeBasedCache
import soil.query.core.UniqueId
import soil.query.core.epoch
import kotlin.time.Duration

typealias SubscriptionCache = TimeBasedCache<UniqueId, SubscriptionState<*>>

/**
 * Creates a new subscription cache.
 */
fun SubscriptionCache(capacity: Int = 20): SubscriptionCache {
    return TimeBasedCache(capacity)
}

/**
 * Builder for creating a subscription cache.
 */
interface SubscriptionCacheBuilder {

    /**
     * Puts the subscription data into the cache.
     *
     * @param id Unique identifier for the subscription.
     * @param data Data to store.
     * @param dataUpdatedAt Timestamp when the data was updated. Default is the current epoch time.
     * @param ttl Time to live for the data. Default is [Duration.INFINITE]
     */
    fun <T> put(
        id: SubscriptionId<T>,
        data: T,
        dataUpdatedAt: Long = epoch(),
        ttl: Duration = Duration.INFINITE
    )
}

/**
 * Creates a new subscription cache with the specified [capacity] and applies the [block] to the builder.
 *
 * ```kotlin
 * val cache = SubscriptionCacheBuilder {
 *    put(UserSubscriptionKey.Id(userId), user)
 *    ..
 * }
 * ```
 */
@Suppress("FunctionName")
fun SubscriptionCacheBuilder(capacity: Int = 20, block: SubscriptionCacheBuilder.() -> Unit): SubscriptionCache {
    return DefaultSubscriptionCacheBuilder(capacity).apply(block).build()
}

internal class DefaultSubscriptionCacheBuilder(capacity: Int) : SubscriptionCacheBuilder {
    private val cache = SubscriptionCache(capacity)

    override fun <T> put(
        id: SubscriptionId<T>,
        data: T,
        dataUpdatedAt: Long,
        ttl: Duration
    ) = cache.set(id, SubscriptionState.success(data, dataUpdatedAt), ttl)

    fun build(): SubscriptionCache {
        return cache
    }
}
