// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import app.cash.turbine.test
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import soil.query.core.ErrorRelay
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.core.RetryFn
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class QueryCommandTest : UnitTest() {

    @Test
    fun testShouldFetch_withInitialState() {
        val ctx = TestQueryContext(
            state = QueryState.initial()
        )
        assertTrue(ctx.shouldFetch())
    }

    @Test
    fun testShouldFetch_withRevision() {
        val ctx = TestQueryContext(
            state = QueryState.success("test")
        )
        assertFalse(ctx.shouldFetch(revision = ctx.state.revision + "dummy"))
    }

    @Test
    fun testShouldFetch_withPausedState() {
        val ctx = TestQueryContext(
            state = QueryState.test(
                error = Exception("Test"),
                status = QueryStatus.Failure,
                fetchStatus = QueryFetchStatus.Paused(Long.MAX_VALUE)
            )
        )
        assertFalse(ctx.shouldFetch())
    }

    @Test
    fun testShouldFetch_withInvalidatedState() {
        val ctx = TestQueryContext(
            state = QueryState.test(
                reply = Reply.some("Test"),
                status = QueryStatus.Success,
                staleAt = Long.MAX_VALUE,
                isInvalidated = true
            )
        )
        assertTrue(ctx.shouldFetch())
    }

    @Test
    fun testShouldFetch_withStaledState() {
        val ctx = TestQueryContext(
            state = QueryState.test(
                reply = Reply.some("Test"),
                status = QueryStatus.Success,
                staleAt = Long.MIN_VALUE
            )
        )
        assertTrue(ctx.shouldFetch())
    }

    @Test
    fun testShouldFetch_withFreshState() {
        val ctx = TestQueryContext(
            state = QueryState.test(
                reply = Reply.some("Test"),
                status = QueryStatus.Success,
                staleAt = Long.MAX_VALUE
            )
        )
        assertFalse(ctx.shouldFetch())
    }

    @Test
    fun testShouldPause() {
        val ctx = TestQueryContext()
        assertNull(ctx.shouldPause(Exception("Test")))
    }

    @Test
    fun testShouldPause_withPauseDurationAfter() {
        val ctx = TestQueryContext(
            options = QueryOptions(
                pauseDurationAfter = { 10.minutes }
            )
        )
        val actual = ctx.shouldPause(Exception("Test"))
        assertNotNull(actual)
        assertTrue(actual.unpauseAt > 0)
    }

    @Test
    fun testFetch_success() = runTest {
        val ctx = TestQueryContext()
        val key = TestQueryKey(mockFetch = { "test-data" })
        val job = launch {
            val result = ctx.fetch(key, TestNonRetry)
            assertTrue(result.isSuccess)
            assertEquals("test-data", result.getOrThrow())
        }
        job.join()
        assertTrue(job.isCompleted)
    }

    @Test
    fun testFetch_failure() = runTest {
        val ctx = TestQueryContext()
        val key = TestQueryKey(mockFetch = { error("Test") })
        val job = launch {
            val result = ctx.fetch(key, TestNonRetry)
            assertTrue(result.isFailure)
            assertEquals("Test", result.exceptionOrNull()?.message)
        }
        job.join()
        assertTrue(job.isCompleted)
    }

    @Test
    fun testFetch_cancel() = runTest {
        val ctx = TestQueryContext()
        val key = TestQueryKey(mockFetch = { throw CancellationException("Test") })
        val job = launch {
            ctx.fetch(key, TestNonRetry)
        }
        job.join()
        assertTrue(job.isCancelled)
    }

    @Test
    fun testDispatchFetchResult_success() = runTest {
        var dispatchedAction: QueryAction<String>? = null
        val ctx = TestQueryContext(
            dispatch = { action -> dispatchedAction = action }
        )
        val key = TestQueryKey(mockFetch = { "test-data" })
        val marker = Marker.None
        var callbackResult: Result<String>? = null
        val callback: QueryCallback<String> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchFetchResult(key, marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is QueryAction.FetchSuccess)
        assertTrue(callbackResult?.isSuccess == true)

        val actionData = (dispatchedAction as QueryAction.FetchSuccess).data
        assertEquals("test-data", actionData)
    }

    @Test
    fun testDispatchFetchResult_failure() = runTest {
        val errorRelay = ErrorRelay.newAnycast(backgroundScope)
        var dispatchedAction: QueryAction<String>? = null
        val ctx = TestQueryContext(
            dispatch = { action -> dispatchedAction = action },
            relay = errorRelay::send
        )
        val key = TestQueryKey(mockFetch = { error("Test") })
        val marker = Marker.None
        var callbackResult: Result<String>? = null
        val callback: QueryCallback<String> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchFetchResult(key, marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is QueryAction.FetchFailure)
        assertTrue(callbackResult?.isFailure == true)
        val job2 = launch {
            errorRelay.receiveAsFlow().test {
                assertEquals(TestQueryKey.Id, awaitItem().keyId)
            }
        }
        job2.join()
    }

    @Test
    fun testDispatchFetchResult_failure_withRecoverData() = runTest {
        val errorRelay = ErrorRelay.newAnycast(backgroundScope)
        var dispatchedAction: QueryAction<String>? = null
        val ctx = TestQueryContext(
            dispatch = { action -> dispatchedAction = action },
            relay = errorRelay::send
        )
        val key = TestQueryKey(
            mockFetch = { error("Test") },
            mockRecoverData = { "recovered-data" }
        )
        val marker = Marker.None
        var callbackResult: Result<String>? = null
        val callback: QueryCallback<String> = { result ->
            callbackResult = result
        }
        val job = launch {
            ctx.dispatchFetchResult(key, marker, callback)
        }
        job.join()
        assertTrue(dispatchedAction is QueryAction.FetchSuccess)
        assertTrue(callbackResult?.isSuccess == true)

        val actionData = (dispatchedAction as QueryAction.FetchSuccess).data
        assertEquals("recovered-data", actionData)
    }

    private object TestNonRetry : RetryFn<String> {
        override suspend fun withRetry(block: suspend () -> String): String = block()
    }

    private class TestQueryKey(
        private val mockFetch: suspend () -> String = { "test-data" },
        private val mockRecoverData: QueryRecoverData<String>? = null
    ) : QueryKey<String> by buildQueryKey(
        id = Id,
        fetch = { mockFetch() }
    ) {
        override fun onRecoverData(): QueryRecoverData<String>? = mockRecoverData

        object Id : QueryId<String>(
            namespace = "test-query"
        )
    }

    private class TestQueryContext(
        override val state: QueryModel<String> = QueryState.initial(),
        override val dispatch: QueryDispatch<String> = {},
        override val options: QueryOptions = QueryOptions(),
        override val relay: QueryErrorRelay? = null,
        override val receiver: QueryReceiver = QueryReceiver {}
    ) : QueryCommand.Context<String>
}
