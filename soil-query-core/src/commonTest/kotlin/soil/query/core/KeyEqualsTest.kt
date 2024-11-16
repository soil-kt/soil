// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import soil.query.QueryId
import soil.query.QueryKey
import soil.query.buildQueryKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class KeyEqualsTest {

    @Test
    fun test() {
        val key1 = TestKey1()
        val key2 = TestKey1()
        assertEquals(key1, key2)
    }

    @Test
    fun testWithoutKeyEquals() {
        val key1 = TestKey2()
        val key2 = TestKey2()
        assertNotEquals(key1, key2)
    }

    private class TestKey1 : KeyEquals(), QueryKey<String> by buildQueryKey(
        id = QueryId("test"),
        fetch = { "test" }
    )

    private class TestKey2 : QueryKey<String> by buildQueryKey(
        id = QueryId("test"),
        fetch = { "test" }
    )
}
