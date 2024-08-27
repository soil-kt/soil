// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PriorityQueueTest : UnitTest() {

    @Test
    fun testSorting() {
        val queue = PriorityQueue<Int>(10)
        queue.push(3)
        queue.push(1)
        queue.push(2)
        queue.push(5)
        queue.push(4)
        assertEquals(1, queue.pop())
        assertEquals(2, queue.pop())
        assertEquals(3, queue.pop())
        assertEquals(4, queue.pop())
        assertEquals(5, queue.pop())
        assertNull(queue.pop())
    }

    @Test
    fun testRemove() {
        val queue = PriorityQueue<Int>(10)
        queue.push(3)
        queue.push(1)
        queue.push(2)
        queue.push(5)
        queue.push(4)
        assertTrue(queue.remove(3))
        assertFalse(queue.remove(3))
        assertEquals(1, queue.pop())
        assertEquals(2, queue.pop())
        assertEquals(4, queue.pop())
        assertEquals(5, queue.pop())
        assertNull(queue.pop())
    }

    @Test
    fun testPeek() {
        val queue = PriorityQueue<Int>(10)
        queue.push(3)
        queue.push(1)
        queue.push(2)
        queue.push(5)
        queue.push(4)
        assertEquals(1, queue.peek())
        assertEquals(1, queue.peek())
        assertEquals(1, queue.pop())
        assertEquals(2, queue.peek())
        assertEquals(2, queue.pop())
        assertEquals(3, queue.peek())
        assertEquals(3, queue.pop())
        assertEquals(4, queue.peek())
        assertEquals(4, queue.pop())
        assertEquals(5, queue.peek())
        assertEquals(5, queue.pop())
        assertNull(queue.peek())
        assertNull(queue.pop())
    }

    @Test
    fun testEmpty() {
        val queue = PriorityQueue<Int>(10)
        assertTrue(queue.isEmpty())
        assertFalse(queue.isNotEmpty())
        assertNull(queue.peek())
        assertNull(queue.pop())
    }

    @Test
    fun testClean() {
        val queue = PriorityQueue<Int>(10)
        queue.push(3)
        queue.push(1)
        queue.push(2)
        queue.push(5)
        queue.push(4)
        queue.clear()
        assertTrue(queue.isEmpty())
        assertFalse(queue.isNotEmpty())
        assertNull(queue.peek())
        assertNull(queue.pop())
    }
}
