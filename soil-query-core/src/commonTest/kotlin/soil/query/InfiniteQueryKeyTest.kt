package soil.query

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.time.Duration.Companion.seconds

class InfiniteQueryKeyTest : UnitTest() {

    @Test
    fun testFetch() = runTest {
        val testKey = TestQueryKey()
        val testClient = SwrCache(backgroundScope)
        val actual = with(testKey) { testClient.queryReceiver.fetch(testKey.initialParam()) }
        assertEquals("Load#0", actual)
    }

    @Test
    fun testContentEquals() = runTest {
        val testKey = TestQueryKey()
        val oldData = buildChunks {
            add(QueryChunk("foo", 0))
        }
        val newData = buildChunks {
            add(QueryChunk("foo", 0))
        }
        val emptyData = emptyChunks<String, Int>()
        assertEquals(testKey.contentEquals(oldData, newData), true)
        assertEquals(testKey.contentEquals(oldData, emptyData), false)
    }

    @Test
    fun testContentCacheable() = runTest {
        val testKey = TestQueryKey()
        val notEmptyData = buildChunks {
            add(QueryChunk("foo", 0))
        }
        val emptyData = emptyChunks<String, Int>()
        assertEquals(testKey.contentCacheable(notEmptyData), true)
        assertEquals(testKey.contentCacheable(emptyData), false)
    }

    @Test
    fun testConfigureOptionsOverride() = runTest {
        val testKey = TestQueryKey()
        val testOptions = QueryOptions(staleTime = 0.seconds)
        val actual = testKey.onConfigureOptions().invoke(testOptions)
        assertEquals(actual.staleTime, 5.seconds)
    }

    @Test
    fun testRecoverData() = runTest {
        val testKey = TestQueryKey()
        val recoveredData = testKey.onRecoverData().invoke(RuntimeException())
        assertEquals(recoveredData.first().data, "Recovered")
        val err = assertFails { testKey.onRecoverData().invoke(Exception("test")) }
        assertEquals(err.message, "test")
    }

    class TestQueryKey : InfiniteQueryKey<String, Int> by buildInfiniteQueryKey(
        id = InfiniteQueryId("test"),
        fetch = { param ->
            delay(1000)
            "Load#$param"
        },
        initialParam = { 0 },
        loadMoreParam = { it.lastOrNull()?.let { chunk -> chunk.param + 1 } }
    ) {
        override val contentEquals: QueryContentEquals<QueryChunks<String, Int>> = { a, b ->
            a == b
        }

        override val contentCacheable: QueryContentCacheable<QueryChunks<String, Int>> = {
            it.map { chunk -> chunk.data }.isNotEmpty()
        }

        override fun onConfigureOptions(): QueryOptionsOverride = { options ->
            options.copy(staleTime = 5.seconds)
        }

        override fun onRecoverData(): QueryRecoverData<QueryChunks<String, Int>> = { err ->
            if (err is RuntimeException) {
                buildChunks {
                    add(QueryChunk("Recovered", 0))
                }
            } else {
                throw err
            }
        }
    }
}
