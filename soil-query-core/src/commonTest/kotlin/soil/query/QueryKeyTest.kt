// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.time.Duration.Companion.seconds

class QueryKeyTest : UnitTest() {

    @Test
    fun testFetch() = runTest {
        val testKey = TestQueryKey()
        val testClient = SwrCache(backgroundScope)
        val actual = with(testKey) { testClient.queryReceiver.fetch() }
        assertEquals("Hello, World!", actual)
    }

    @Test
    fun testContentEquals() = runTest {
        val testKey = TestQueryKey()
        assertEquals(testKey.contentEquals("foo", "foo"), true)
        assertEquals(testKey.contentEquals("foo", "bar"), false)
    }

    @Test
    fun testContentCacheable() = runTest {
        val testKey = TestQueryKey()
        assertEquals(testKey.contentCacheable("Hello, World!"), true)
        assertEquals(testKey.contentCacheable(""), false)
    }

    @Test
    fun testConfigureOptionsOverride() = runTest {
        val testKey = TestQueryKey()
        val testOptions = QueryOptions(staleTime = 0.seconds)
        val actual = testKey.onConfigureOptions().invoke(testOptions)
        assertEquals(actual.staleTime, 5.seconds)
    }

    @Test
    fun testInitialData() = runTest {
        val testKey = TestQueryKey()
        val testClient = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                queryCache = QueryCacheBuilder {
                    put(QueryId("sample"), "Hello, Soil!")
                }
            )
        )
        val actual = testKey.onInitialData().invoke(testClient)
        assertEquals(actual, "Hello, Soil!")
    }

    @Test
    fun testPreloadData() = runTest {
        val testKey = TestQueryKey()
        val testClient = SwrCache(backgroundScope)
        val actual = testKey.onPreloadData().invoke(testClient.queryReceiver)
        assertEquals("Preloaded", actual)
    }

    @Test
    fun testRecoverData() = runTest {
        val testKey = TestQueryKey()
        assertEquals(testKey.onRecoverData().invoke(RuntimeException()), "Recovered")
        val err = assertFails { testKey.onRecoverData().invoke(Exception("test")) }
        assertEquals(err.message, "test")
    }

    class TestQueryKey : QueryKey<String> by buildQueryKey(
        id = QueryId("test"),
        fetch = {
            delay(1000)
            "Hello, World!"
        }
    ) {
        override val contentEquals: QueryContentEquals<String> = { a, b ->
            a == b
        }

        override val contentCacheable: QueryContentCacheable<String> = {
            it.isNotEmpty()
        }

        override fun onConfigureOptions(): QueryOptionsOverride = { options ->
            options.copy(staleTime = 5.seconds)
        }

        override fun onInitialData(): QueryInitialData<String> = {
            getQueryData(QueryId("sample"))
        }

        override fun onPreloadData(): QueryPreloadData<String> = {
            "Preloaded"
        }

        override fun onRecoverData(): QueryRecoverData<String> = { err ->
            if (err is RuntimeException) {
                "Recovered"
            } else {
                throw err
            }
        }
    }
}
