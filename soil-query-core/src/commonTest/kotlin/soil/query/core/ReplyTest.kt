// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ReplyTest : UnitTest() {

    @Test
    fun testNone() {
        val reply = Reply.none<Int>()
        assertTrue(reply.isNone)
        assertFailsWith<IllegalStateException> { reply.getOrThrow() }
        assertNull(reply.getOrNull())
        assertEquals(0, reply.getOrElse { 0 })
        assertEquals(0, reply.map { it + 1 }.getOrElse { 0 })
    }

    @Test
    fun testSome() {
        val reply = Reply.some(1)
        assertTrue(!reply.isNone)
        assertEquals(1, reply.getOrThrow())
        assertEquals(1, reply.getOrNull())
        assertEquals(1, reply.getOrElse { 0 })
        assertEquals(2, reply.map { it + 1 }.getOrThrow())
    }

    @Test
    fun testCompanion_combinePair() {
        val reply1 = Reply.some(1)
        val reply2 = Reply.some(2)
        val reply3 = Reply.none<Int>()

        assertEquals(3, Reply.combine(reply1, reply2) { a, b -> a + b }.getOrThrow())
        assertTrue(Reply.combine(reply1, reply3) { a, b -> a + b }.isNone)
        assertTrue(Reply.combine(reply2, reply3) { a, b -> a + b }.isNone)
        assertTrue(Reply.combine(reply3, reply3) { a, b -> a + b }.isNone)
    }

    @Test
    fun testCompanion_combineTriple() {
        val reply1 = Reply.some(1)
        val reply2 = Reply.some(2)
        val reply3 = Reply.some(3)
        val reply4 = Reply.none<Int>()

        assertEquals(6, Reply.combine(reply1, reply2, reply3) { a, b, c -> a + b + c }.getOrThrow())
        assertTrue(Reply.combine(reply1, reply2, reply4) { a, b, c -> a + b + c }.isNone)
        assertTrue(Reply.combine(reply2, reply3, reply4) { a, b, c -> a + b + c }.isNone)
        assertTrue(Reply.combine(reply4, reply4, reply4) { a, b, c -> a + b + c }.isNone)
    }
}
