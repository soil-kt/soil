// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlin.time.Duration

/**
 * A time-based cache that evicts entries based on their time-to-live (TTL).
 *
 * @param K The type of keys maintained by this cache.
 * @param V The type of mapped values.
 * @property capacity The maximum number of entries that this cache can hold.
 * @property time A function that returns the current time in seconds since the epoch.
 * @constructor Creates a new time-based cache with the specified capacity and time function.
 */
class TimeBasedCache<K : Any, V : Any>(
    private val capacity: Int,
    private val time: () -> Long = { epoch() }
) {

    private val cache = LinkedHashMap<K, Item<K, V>>(capacity)
    private val queue = PriorityQueue<Item<K, V>>(capacity)

    /**
     * Returns the number of entries in this cache.
     */
    val size: Int
        get() = cache.size

    /**
     * Returns the keys contained in this cache.
     */
    val keys: Set<K>
        get() = cache.keys

    /**
     * Returns the value of the specified key in this cache
     *
     * @return `null` if the key is not found or has expired.
     */
    operator fun get(key: K): V? {
        val item = cache[key]
        if (item != null && time() < item.expires) {
            return item.value
        }
        return null
    }

    /**
     * Save the specified key with value in this cache.
     *
     * - If the cache is full, the oldest entry will be evicted.
     * - If the key already exists, delete the old entry and save the new entry.
     */
    fun set(key: K, value: V, ttl: Duration) {
        val now = time()
        if (cache.containsKey(key)) {
            delete(key)
        } else if (cache.size >= capacity) {
            evict(now)
        }

        val expires = ttl.toEpoch(now)
        val item = Item(key, value, expires)
        cache[key] = item
        queue.push(item)
    }

    /**
     * Updates the value associated with the specified key in this cache.
     */
    fun swap(key: K, edit: V.() -> V) {
        val current = cache[key] ?: return
        val changed = current.copy(value = current.value.edit())
        cache[key] = changed
        queue.remove(current)
        queue.push(changed)
    }

    /**
     * Evicts entries that have expired.
     */
    fun evict(now: Long = time()) {
        if (cache.isEmpty()) {
            return
        }
        val size = cache.size
        while (cache.isNotEmpty() && queue.isNotEmpty()) {
            val item = queue.peek()!!
            if (item.expires > now) {
                break
            }
            queue.pop()
            cache.remove(item.key)
        }

        if (cache.size == size) {
            val item = queue.pop()!!
            cache.remove(item.key)
        }
    }

    /**
     * Removes the entry for the specified key from this cache if it is present.
     */
    fun delete(key: K) {
        val item = cache.remove(key) ?: return
        queue.remove(item)
    }

    /**
     * Removes all entries from this cache.
     */
    fun clear() {
        cache.clear()
        queue.clear()
    }

    override fun toString(): String {
        return "TimeBasedCache[capacity=$capacity, keys=${cache.keys}]"
    }

    /**
     * An item in the cache that holds a key, a value, and an expiration time.
     *
     * @param K The type of keys maintained by this cache
     * @param V The type of mapped values
     * @property key The key of this item
     * @property value The value of this item
     * @property expires The expiration time of this item
     * @constructor Creates a new item with the specified key, value, and expiration time.
     */
    data class Item<K : Any, V : Any>(
        val key: K,
        val value: V,
        val expires: Long
    ) : Comparable<Item<K, V>> {
        override fun compareTo(other: Item<K, V>): Int {
            return expires.compareTo(other.expires)
        }
    }
}
