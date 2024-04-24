// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlin.time.Duration

class TimeBasedCache<K : Any, V : Any>(
    private val capacity: Int,
    private val time: () -> Long = { epoch() }
) {

    private val cache = LinkedHashMap<K, Item<K, V>>(capacity)
    private val queue = PriorityQueue<Item<K, V>>(capacity)

    val size: Int
        get() = cache.size

    val keys: Set<K>
        get() = cache.keys

    operator fun get(key: K): V? {
        val item = cache[key]
        if (item != null && time() < item.expires) {
            return item.value
        }
        return null
    }

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

    fun swap(key: K, edit: V.() -> V) {
        val current = cache[key] ?: return
        val changed = current.copy(value = current.value.edit())
        cache[key] = changed
        queue.remove(current)
        queue.push(changed)
    }


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

    fun delete(key: K) {
        val item = cache.remove(key) ?: return
        queue.remove(item)
    }

    fun clear() {
        cache.clear()
        queue.clear()
    }

    override fun toString(): String {
        return "TimeBasedCache[capacity=$capacity, keys=${cache.keys}]"
    }

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
