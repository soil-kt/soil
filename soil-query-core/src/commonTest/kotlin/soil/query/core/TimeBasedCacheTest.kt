// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TimeBasedCacheTest : UnitTest() {

    @Test
    fun testCache() {
        val clock = TestClock()
        val cache = TimeBasedCache<String, String>(
            capacity = 10,
            time = clock::epoch
        )
        cache.set("key1", "value1", ttl = 10.seconds)
        cache.set("key2", "value2", ttl = 30.seconds)

        assertEquals(2, cache.size)
        assertEquals(setOf("key1", "key2"), cache.keys)
        assertEquals("value1", cache["key1"])
        assertEquals("value2", cache["key2"])

        clock.advance(20.seconds)
        assertEquals(2, cache.size)
        assertEquals(setOf("key1", "key2"), cache.keys)
        assertNull(cache["key1"])
        assertEquals("value2", cache["key2"])

        clock.advance(20.seconds)
        assertEquals(2, cache.size)
        assertEquals(setOf("key1", "key2"), cache.keys)
        assertNull(cache["key1"])
        assertNull(cache["key2"])

        cache.evict()
        assertEquals(0, cache.size)
        assertEquals(emptySet(), cache.keys)
    }

    @Test
    fun testSet_containsKey() {
        val clock = TestClock()
        val cache = TimeBasedCache<String, String>(
            capacity = 10,
            time = clock::epoch
        )
        cache.set("key1", "value1", ttl = 10.seconds)
        cache.set("key1", "value2", ttl = 20.seconds)

        assertEquals("value2", cache["key1"])

        clock.advance(15.seconds)
        assertEquals("value2", cache["key1"])
    }

    @Test
    fun testSet_capacityOver() {
        val clock = TestClock()
        val cache = TimeBasedCache<String, String>(
            capacity = 3,
            time = clock::epoch
        )
        cache.set("key1", "value1", ttl = 10.seconds)
        cache.set("key2", "value2", ttl = 20.seconds)
        cache.set("key3", "value3", ttl = 5.seconds)
        cache.set("key4", "value4", ttl = 15.seconds)

        assertEquals(3, cache.size)
        assertEquals("value1", cache["key1"])
        assertEquals("value2", cache["key2"])
        assertNull(cache["key3"])
        assertEquals("value4", cache["key4"])
    }

    @Test
    fun testSwap() {
        val clock = TestClock()
        val cache = TimeBasedCache<String, String>(
            capacity = 10,
            time = clock::epoch
        )
        cache.set("key1", "value1", ttl = 10.seconds)
        cache.swap("key1") { "value2" }

        assertEquals("value2", cache["key1"])
    }

    @Test
    fun testEvict_expired() {
        val clock = TestClock()
        val cache = TimeBasedCache<String, String>(
            capacity = 5,
            time = clock::epoch
        )

        cache.evict()

        cache.set("key1", "value1", ttl = 10.seconds)
        cache.evict()
        assertEquals(1, cache.size)
        assertEquals("value1", cache["key1"])

        clock.advance(11.seconds)
        cache.evict()
        assertEquals(0, cache.size)
        assertNull(cache["key1"])
    }

    @Test
    fun testDelete() {
        val clock = TestClock()
        val cache = TimeBasedCache<String, String>(
            capacity = 5,
            time = clock::epoch
        )

        cache.set("key1", "value1", ttl = 10.seconds)
        cache.delete("key1")
        assertEquals(0, cache.size)
        assertNull(cache["key1"])
    }

    @Test
    fun testClear() {
        val clock = TestClock()
        val cache = TimeBasedCache<String, String>(
            capacity = 5,
            time = clock::epoch
        )

        cache.set("key1", "value1", ttl = 10.seconds)
        cache.set("key2", "value2", ttl = 20.seconds)
        cache.clear()
        assertEquals(0, cache.size)
        assertNull(cache["key1"])
        assertNull(cache["key2"])
    }

    class TestClock {
        private var time: Long = TEST_CLOCK_EPOCH

        fun advance(duration: Duration) {
            time += duration.inWholeSeconds
        }

        fun epoch(): Long = time
    }

    companion object {
        // Epoch time: 2024-08-01T00:00:00Z
        private const val TEST_CLOCK_EPOCH: Long = 1722470400
    }

}
