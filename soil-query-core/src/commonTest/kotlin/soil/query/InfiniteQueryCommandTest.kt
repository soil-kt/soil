// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import soil.query.core.ErrorRelay
import soil.query.core.Marker
import soil.query.core.RetryFn
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class InfiniteQueryCommandTest : UnitTest() {

    @Test
    fun testFetch_success() = runTest {
        val ctx = TestInfiniteQueryContext()
        val key = TestInfiniteQueryKey(mockFetch = { "test-data-$it" })
        val variable = 0
        val job = launch {
            val result = ctx.fetch(key, variable, TestNonRetry)
            assertTrue(result.isSuccess)
            assertEquals("test-data-$variable", result.getOrThrow())
        }
        job.join()
        assertTrue(job.isCompleted)
    }

    @Test
    fun testFetch_failure() = runTest {
        val ctx = TestInfiniteQueryContext()
        val key = TestInfiniteQueryKey(mockFetch = { error("Test") })
        val variable = 0
        val job = launch {
            val result = ctx.fetch(key, variable, TestNonRetry)
            assertTrue(result.isFailure)
            assertEquals("Test", result.exceptionOrNull()?.message)
        }
        job.join()
        assertTrue(job.isCompleted)
    }

    @Test
    fun testFetch_cancel() = runTest {
        val ctx = TestInfiniteQueryContext()
        val key = TestInfiniteQueryKey(mockFetch = { throw CancellationException("Test") })
        val variable = 0
        val job = launch {
            ctx.fetch(key, variable, TestNonRetry)
        }
        job.join()
        assertTrue(job.isCancelled)
    }

    @Test
    fun testRevalidate_success() = runTest {
        val ctx = TestInfiniteQueryContext()
        val key = TestInfiniteQueryKey(mockFetch = { "test-data-$it" })
        val initialChunks = buildChunks {
            add(QueryChunk("test-data-0", 0))
            add(QueryChunk("test-data-1", 1))
        }
        val job = launch {
            val result = ctx.revalidate(key, initialChunks)
            assertTrue(result.isSuccess)
            val chunks = result.getOrNull()
            assertNotNull(chunks)
            assertEquals(2, chunks.size)
            assertEquals("test-data-0", chunks[0].data)
            assertEquals(0, chunks[0].param)
            assertEquals("test-data-1", chunks[1].data)
            assertEquals(1, chunks[1].param)
        }
        job.join()
        assertTrue(job.isCompleted)
    }

    @Test
    fun testRevalidate_success_whenDataCountDecreases() = runTest {
        val ctx = TestInfiniteQueryContext()
        val key = TestInfiniteQueryKey(
            mockFetch = { "test-data-$it" },
            mockLoadMoreParam = { null }
        )
        val initialChunks = buildChunks {
            add(QueryChunk("test-data-0", 0))
            add(QueryChunk("test-data-1", 1))
        }
        val job = launch {
            val result = ctx.revalidate(key, initialChunks)
            assertTrue(result.isSuccess)
            val chunks = result.getOrNull()
            assertNotNull(chunks)
            assertEquals(1, chunks.size)
            assertEquals("test-data-0", chunks[0].data)
            assertEquals(0, chunks[0].param)
        }
        job.join()
        assertTrue(job.isCompleted)
    }

    @Test
    fun testRevalidate_failure() = runTest {
        val ctx = TestInfiniteQueryContext()
        val key = TestInfiniteQueryKey(mockFetch = { error("Test") })
        val initialChunks = buildChunks {
            add(QueryChunk("test-data-0", 0))
            add(QueryChunk("test-data-1", 1))
        }
        val job = launch {
            val result = ctx.revalidate(key, initialChunks)
            assertTrue(result.isFailure)
            assertEquals("Test", result.exceptionOrNull()?.message)
        }
        job.join()
        assertTrue(job.isCompleted)
    }

    @Test
    fun testRevalidate_cancel() = runTest {
        val ctx = TestInfiniteQueryContext()
        val key = TestInfiniteQueryKey(mockFetch = { throw CancellationException("Test") })
        val initialChunks = buildChunks {
            add(QueryChunk("test-data-0", 0))
            add(QueryChunk("test-data-1", 1))
        }
        val job = launch {
            ctx.revalidate(key, initialChunks)
        }
        job.join()
        assertTrue(job.isCancelled)
    }

    @Test
    fun testDispatchFetchChunksResult_success() = runTest {
        var dispatchedAction: QueryAction<QueryChunks<String, Int>>? = null
        val ctx = TestInfiniteQueryContext(
            dispatch = { action -> dispatchedAction = action }
        )
        val key = TestInfiniteQueryKey(mockFetch = { "test-data-$it" })
        val variable = 0
        val marker = Marker.None
        var callbackResult: Result<QueryChunks<String, Int>>? = null
        val callback: QueryCallback<QueryChunks<String, Int>> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchFetchChunksResult(key, variable, marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is QueryAction.FetchSuccess)
        assertTrue(callbackResult?.isSuccess == true)

        val actionData = (dispatchedAction as QueryAction.FetchSuccess).data
        assertEquals(1, actionData.size)
        assertEquals("test-data-0", actionData[0].data)
        assertEquals(0, actionData[0].param)
    }

    @Test
    fun testDispatchFetchChunksResult_success_whenLoadMore() = runTest {
        var dispatchedAction: QueryAction<QueryChunks<String, Int>>? = null
        val ctx = TestInfiniteQueryContext(
            state = QueryState.success(data = buildChunks {
                add(QueryChunk("test-data-0", 0))
            }),
            dispatch = { action -> dispatchedAction = action }
        )
        val key = TestInfiniteQueryKey(mockFetch = { "test-data-$it" })
        val variable = 1
        val marker = Marker.None
        var callbackResult: Result<QueryChunks<String, Int>>? = null
        val callback: QueryCallback<QueryChunks<String, Int>> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchFetchChunksResult(key, variable, marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is QueryAction.FetchSuccess)
        assertTrue(callbackResult?.isSuccess == true)

        val actionData = (dispatchedAction as QueryAction.FetchSuccess).data
        assertEquals(2, actionData.size)
        assertEquals("test-data-0", actionData[0].data)
        assertEquals(0, actionData[0].param)
        assertEquals("test-data-1", actionData[1].data)
        assertEquals(1, actionData[1].param)
    }

    @Test
    fun testDispatchFetchChunksResult_failure() = runTest {
        val errorRelay = ErrorRelay.newAnycast(backgroundScope)
        var dispatchedAction: QueryAction<QueryChunks<String, Int>>? = null
        val ctx = TestInfiniteQueryContext(
            dispatch = { action -> dispatchedAction = action },
            relay = errorRelay::send
        )
        val key = TestInfiniteQueryKey(mockFetch = { error("Test") })
        val variable = 0
        val marker = Marker.None
        var callbackResult: Result<QueryChunks<String, Int>>? = null
        val callback: QueryCallback<QueryChunks<String, Int>> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchFetchChunksResult(key, variable, marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is QueryAction.FetchFailure)
        assertTrue(callbackResult?.isFailure == true)
        val job2 = launch {
            errorRelay.receiveAsFlow().test {
                assertEquals(TestInfiniteQueryKey.Id, awaitItem().keyId)
            }
        }
        job2.join()
    }

    @Test
    fun testDispatchFetchChunksResult_failure_withRecoverData() = runTest {
        val errorRelay = ErrorRelay.newAnycast(backgroundScope)
        var dispatchedAction: QueryAction<QueryChunks<String, Int>>? = null
        val ctx = TestInfiniteQueryContext(
            dispatch = { action -> dispatchedAction = action },
            relay = errorRelay::send
        )
        val key = TestInfiniteQueryKey(
            mockFetch = { error("Test") },
            mockRecoverData = { emptyList() }
        )
        val variable = 0
        val marker = Marker.None
        var callbackResult: Result<QueryChunks<String, Int>>? = null
        val callback: QueryCallback<QueryChunks<String, Int>> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchFetchChunksResult(key, variable, marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is QueryAction.FetchSuccess)
        assertTrue(callbackResult?.isSuccess == true)

        val actionData = (dispatchedAction as QueryAction.FetchSuccess).data
        assertEquals(0, actionData.size)
    }

    @Test
    fun testDispatchRevalidateChunksResult_success() = runTest {
        var dispatchedAction: QueryAction<QueryChunks<String, Int>>? = null
        val ctx = TestInfiniteQueryContext(
            dispatch = { action -> dispatchedAction = action }
        )
        val key = TestInfiniteQueryKey(mockFetch = { "test-data-$it" })
        val initialChunks = buildChunks {
            add(QueryChunk("test-data-0", 0))
            add(QueryChunk("test-data-1", 1))
        }
        val marker = Marker.None
        var callbackResult: Result<QueryChunks<String, Int>>? = null
        val callback: QueryCallback<QueryChunks<String, Int>> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchRevalidateChunksResult(key, initialChunks, marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is QueryAction.FetchSuccess)
        assertTrue(callbackResult?.isSuccess == true)

        val actionData = (dispatchedAction as QueryAction.FetchSuccess).data
        assertEquals(2, actionData.size)
        assertEquals("test-data-0", actionData[0].data)
        assertEquals(0, actionData[0].param)
        assertEquals("test-data-1", actionData[1].data)
        assertEquals(1, actionData[1].param)
    }

    @Test
    fun testDispatchRevalidateChunksResult_failure() = runTest {
        val errorRelay = ErrorRelay.newAnycast(backgroundScope)
        var dispatchedAction: QueryAction<QueryChunks<String, Int>>? = null
        val ctx = TestInfiniteQueryContext(
            dispatch = { action -> dispatchedAction = action },
            relay = errorRelay::send
        )
        val key = TestInfiniteQueryKey(mockFetch = { error("Test") })
        val initialChunks = buildChunks {
            add(QueryChunk("test-data-0", 0))
            add(QueryChunk("test-data-1", 1))
        }
        val marker = Marker.None
        var callbackResult: Result<QueryChunks<String, Int>>? = null
        val callback: QueryCallback<QueryChunks<String, Int>> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchRevalidateChunksResult(key, initialChunks, marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is QueryAction.FetchFailure)
        assertTrue(callbackResult?.isFailure == true)
        val job2 = launch {
            errorRelay.receiveAsFlow().test {
                assertEquals(TestInfiniteQueryKey.Id, awaitItem().keyId)
            }
        }
        job2.join()
    }

    private object TestNonRetry : RetryFn<String> {
        override suspend fun withRetry(block: suspend () -> String): String = block()
    }

    private class TestInfiniteQueryKey(
        private val mockFetch: suspend (Int) -> String = { param ->
            "test-data-$param"
        },
        private val mockLoadMoreParam: (QueryChunks<String, Int>) -> Int? = { chunks ->
            chunks.lastOrNull()?.param?.plus(1)
        },
        private val mockRecoverData: QueryRecoverData<QueryChunks<String, Int>>? = null
    ) : InfiniteQueryKey<String, Int> by buildInfiniteQueryKey(
        id = Id,
        fetch = { mockFetch(it) },
        initialParam = { 0 },
        loadMoreParam = mockLoadMoreParam
    ) {
        override fun onRecoverData(): QueryRecoverData<QueryChunks<String, Int>>? = mockRecoverData

        object Id : InfiniteQueryId<String, Int>(
            namespace = "test-infinite-query"
        )
    }

    private class TestInfiniteQueryContext(
        override val state: QueryModel<QueryChunks<String, Int>> = QueryState.initial(),
        override val dispatch: QueryDispatch<QueryChunks<String, Int>> = {},
        override val options: QueryOptions = QueryOptions(),
        override val relay: QueryErrorRelay? = null,
        override val receiver: QueryReceiver = QueryReceiver {}
    ) : QueryCommand.Context<QueryChunks<String, Int>>
}
