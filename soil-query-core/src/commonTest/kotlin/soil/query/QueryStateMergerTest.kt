// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class QueryStateMergerTest : UnitTest() {

    @Test
    fun testMergeForTwo() {
        val state1 = QueryState.success(1, dataUpdatedAt = 200, dataStaleAt = 250)
        val state2 = QueryState.success(2, dataUpdatedAt = 300, dataStaleAt = 350)
        val merged1 = QueryState.merge(state1, state2) { a, b -> a + b }
        val merged2 = QueryState.merge(state2, state1) { a, b -> a + b }
        assertEquals(QueryState.success(3, 200, 250), merged1)
        assertEquals(QueryState.success(3, 300, 350), merged2)
    }

    @Test
    fun testMergeForTwo_withFailure() {
        val error = RuntimeException("error")
        val state1 = QueryState.success(1, dataUpdatedAt = 200, dataStaleAt = 250)
        val state2 = QueryState.failure<Int>(error, errorUpdatedAt = 999)
        val merged1 = QueryState.merge(state1, state2) { a, b -> a + b }
        val merged2 = QueryState.merge(state2, state1) { a, b -> a + b }
        assertEquals(QueryState.failure(error, 999), merged1)
        assertEquals(QueryState.failure(error, 999), merged2)
    }

    @Test
    fun testMergeForTwo_allFailure() {
        val error = RuntimeException("error")
        val state1 = QueryState.failure<Int>(error, errorUpdatedAt = 555)
        val state2 = QueryState.failure<Int>(error, errorUpdatedAt = 999)
        val merged1 = QueryState.merge(state1, state2) { a, b -> a + b }
        val merged2 = QueryState.merge(state2, state1) { a, b -> a + b }
        assertEquals(QueryState.failure(error, 555), merged1)
        assertEquals(QueryState.failure(error, 555), merged2)
    }

    @Test
    fun testMergeForTwo_withPending() {
        val state1 = QueryState.success(1, dataUpdatedAt = 200, dataStaleAt = 250)
        val state2 = QueryState.initial<Int>()
        val merged1 = QueryState.merge(state1, state2) { a, b -> a + b }
        val merged2 = QueryState.merge(state2, state1) { a, b -> a + b }
        assertEquals(QueryState.initial(), merged1)
        assertEquals(QueryState.initial(), merged2)
    }

    @Test
    fun testMergeForTwo_allPending() {
        val state1 = QueryState.initial<Int>()
        val state2 = QueryState.initial<Int>()
        val merged1 = QueryState.merge(state1, state2) { a, b -> a + b }
        val merged2 = QueryState.merge(state2, state1) { a, b -> a + b }
        assertEquals(QueryState.initial(), merged1)
        assertEquals(QueryState.initial(), merged2)
    }

    @Test
    fun testMergeForThree() {
        val state1 = QueryState.success(1, dataUpdatedAt = 200, dataStaleAt = 250)
        val state2 = QueryState.success(2, dataUpdatedAt = 300, dataStaleAt = 350)
        val state3 = QueryState.success(3, dataUpdatedAt = 400, dataStaleAt = 450)
        val merged1 = QueryState.merge(state1, state2, state3) { a, b, c -> a + b + c }
        val merged2 = QueryState.merge(state2, state1, state3) { a, b, c -> a + b + c }
        assertEquals(QueryState.success(6, 200, 250), merged1)
        assertEquals(QueryState.success(6, 300, 350), merged2)
    }

    @Test
    fun testMergeForThree_withFailure() {
        val error = RuntimeException("error")
        val state1 = QueryState.success(1, dataUpdatedAt = 200, dataStaleAt = 250)
        val state2 = QueryState.failure<Int>(error, errorUpdatedAt = 999)
        val state3 = QueryState.success(3, dataUpdatedAt = 400, dataStaleAt = 450)
        val merged1 = QueryState.merge(state1, state2, state3) { a, b, c -> a + b + c }
        val merged2 = QueryState.merge(state2, state1, state3) { a, b, c -> a + b + c }
        assertEquals(QueryState.failure(error, 999), merged1)
        assertEquals(QueryState.failure(error, 999), merged2)
    }

    @Test
    fun testMergeForThree_allFailure() {
        val error = RuntimeException("error")
        val state1 = QueryState.failure<Int>(error, errorUpdatedAt = 555)
        val state2 = QueryState.failure<Int>(error, errorUpdatedAt = 999)
        val state3 = QueryState.failure<Int>(error, errorUpdatedAt = 777)
        val merged1 = QueryState.merge(state1, state2, state3) { a, b, c -> a + b + c }
        val merged2 = QueryState.merge(state2, state1, state3) { a, b, c -> a + b + c }
        assertEquals(QueryState.failure(error, 555), merged1)
        assertEquals(QueryState.failure(error, 555), merged2)
    }

    @Test
    fun testMergeForThree_withPending() {
        val state1 = QueryState.success(1, dataUpdatedAt = 200, dataStaleAt = 250)
        val state2 = QueryState.initial<Int>()
        val state3 = QueryState.success(3, dataUpdatedAt = 400, dataStaleAt = 450)
        val merged1 = QueryState.merge(state1, state2, state3) { a, b, c -> a + b + c }
        val merged2 = QueryState.merge(state2, state1, state3) { a, b, c -> a + b + c }
        assertEquals(QueryState.initial(), merged1)
        assertEquals(QueryState.initial(), merged2)
    }

    @Test
    fun testMergeForThree_allPending() {
        val state1 = QueryState.initial<Int>()
        val state2 = QueryState.initial<Int>()
        val state3 = QueryState.initial<Int>()
        val merged1 = QueryState.merge(state1, state2, state3) { a, b, c -> a + b + c }
        val merged2 = QueryState.merge(state2, state1, state3) { a, b, c -> a + b + c }
        assertEquals(QueryState.initial(), merged1)
        assertEquals(QueryState.initial(), merged2)
    }

    @Test
    fun testMergeForN() {
        val state1 = QueryState.success(1, dataUpdatedAt = 200, dataStaleAt = 250)
        val state2 = QueryState.success(2, dataUpdatedAt = 300, dataStaleAt = 350)
        val state3 = QueryState.success(3, dataUpdatedAt = 400, dataStaleAt = 450)
        val merged1 = QueryState.merge(arrayOf(state1, state2, state3)) { it.reduce { acc, i -> acc + i } }
        val merged2 = QueryState.merge(arrayOf(state2, state1, state3)) { it.reduce { acc, i -> acc + i } }
        assertEquals(QueryState.success(6, 200, 250), merged1)
        assertEquals(QueryState.success(6, 300, 350), merged2)
    }

    @Test
    fun testMergeForN_withFailure() {
        val error = RuntimeException("error")
        val state1 = QueryState.success(1, dataUpdatedAt = 200, dataStaleAt = 250)
        val state2 = QueryState.failure<Int>(error, errorUpdatedAt = 999)
        val state3 = QueryState.success(3, dataUpdatedAt = 400, dataStaleAt = 450)
        val merged1 = QueryState.merge(arrayOf(state1, state2, state3)) { it.reduce { acc, i -> acc + i } }
        val merged2 = QueryState.merge(arrayOf(state2, state1, state3)) { it.reduce { acc, i -> acc + i } }
        assertEquals(QueryState.failure(error, 999), merged1)
        assertEquals(QueryState.failure(error, 999), merged2)
    }

    @Test
    fun testMergeForN_allFailure() {
        val error = RuntimeException("error")
        val state1 = QueryState.failure<Int>(error, errorUpdatedAt = 555)
        val state2 = QueryState.failure<Int>(error, errorUpdatedAt = 999)
        val state3 = QueryState.failure<Int>(error, errorUpdatedAt = 777)
        val merged1 = QueryState.merge(arrayOf(state1, state2, state3)) { it.reduce { acc, i -> acc + i } }
        val merged2 = QueryState.merge(arrayOf(state2, state1, state3)) { it.reduce { acc, i -> acc + i } }
        assertEquals(QueryState.failure(error, 555), merged1)
        assertEquals(QueryState.failure(error, 555), merged2)
    }

    @Test
    fun testMergeForN_withPending() {
        val state1 = QueryState.success(1, dataUpdatedAt = 200, dataStaleAt = 250)
        val state2 = QueryState.initial<Int>()
        val state3 = QueryState.success(3, dataUpdatedAt = 400, dataStaleAt = 450)
        val merged1 = QueryState.merge(arrayOf(state1, state2, state3)) { it.reduce { acc, i -> acc + i } }
        val merged2 = QueryState.merge(arrayOf(state2, state1, state3)) { it.reduce { acc, i -> acc + i } }
        assertEquals(QueryState.initial(), merged1)
        assertEquals(QueryState.initial(), merged2)
    }

    @Test
    fun testMergeForN_allPending() {
        val state1 = QueryState.initial<Int>()
        val state2 = QueryState.initial<Int>()
        val state3 = QueryState.initial<Int>()
        val merged1 = QueryState.merge(arrayOf(state1, state2, state3)) { it.reduce { acc, i -> acc + i } }
        val merged2 = QueryState.merge(arrayOf(state2, state1, state3)) { it.reduce { acc, i -> acc + i } }
        assertEquals(QueryState.initial(), merged1)
        assertEquals(QueryState.initial(), merged2)
    }
}
