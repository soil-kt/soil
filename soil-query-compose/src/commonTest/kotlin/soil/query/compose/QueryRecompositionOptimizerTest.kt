// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.material.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import soil.query.QueryFetchStatus
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryState
import soil.query.QueryStatus
import soil.query.SwrCache
import soil.query.buildQueryKey
import soil.query.core.Reply
import soil.query.test.test
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class QueryRecompositionOptimizerTest : UnitTest() {

    @Test
    fun testRecompositionCount_default() = runUiTest { testScope ->
        val key = TestQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test()
        var recompositionCount = 0
        setContent {
            SwrClientProvider(client) {
                val query = rememberQuery(key)
                SideEffect { recompositionCount++ }
                when (val reply = query.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
                    is Reply.None -> Unit
                }
            }
        }
        waitForIdle()
        testScope.runCurrent()
        waitForIdle()
        testScope.advanceTimeBy(1000)
        waitUntilExactlyOneExists(hasTestTag("query"))

        // pending -> success
        assertEquals(2, recompositionCount)
    }

    @Test
    fun testRecompositionCount_disabled() = runUiTest { testScope ->
        val key = TestQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test()
        var recompositionCount = 0
        setContent {
            SwrClientProvider(client) {
                val query = rememberQuery(key, config = QueryConfig {
                    optimizer = QueryRecompositionOptimizer.Disabled
                })
                SideEffect { recompositionCount++ }
                when (val reply = query.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
                    is Reply.None -> Unit
                }
            }
        }
        waitForIdle()
        testScope.runCurrent()
        waitForIdle()
        testScope.advanceTimeBy(1000)
        waitUntilExactlyOneExists(hasTestTag("query"))

        // pending -> pending(fetching) -> success
        assertEquals(3, recompositionCount)
    }

    @Test
    fun testOmit_pending() {
        val state = QueryState.test(
            reply = Reply.None,
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Pending,
            fetchStatus = QueryFetchStatus.Fetching(),
            isInvalidated = false
        )
        val actual = QueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test(
            reply = Reply.None,
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            staleAt = 0,
            status = QueryStatus.Pending,
            fetchStatus = QueryFetchStatus.Idle,
            isInvalidated = false
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_success() {
        val state = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching(),
            isInvalidated = false
        )
        val actual = QueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            staleAt = 0,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching(),
            isInvalidated = false
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_success_isValidating() {
        val state = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching(isValidating = true),
            isInvalidated = false
        )
        val actual = QueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            staleAt = 0,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Idle,
            isInvalidated = false
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_success_isInvalidated() {
        val state = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching(isValidating = true),
            isInvalidated = true
        )
        val actual = QueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            staleAt = 0,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching(isValidating = true),
            isInvalidated = true
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_failure() {
        val error = RuntimeException("error")
        val state = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching(),
            isInvalidated = false
        )
        val actual = QueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 0,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching(),
            isInvalidated = false
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_failure_isValidating() {
        val error = RuntimeException("error")
        val state = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching(isValidating = true),
            isInvalidated = false
        )
        val actual = QueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 0,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching(isValidating = true),
            isInvalidated = false
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_failure_isInvalidated() {
        val error = RuntimeException("error")
        val state = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching(isValidating = true),
            isInvalidated = true
        )
        val actual = QueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 0,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching(isValidating = true),
            isInvalidated = true
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_pending() {
        val expected = QueryState.test(
            reply = Reply.None,
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Pending,
            fetchStatus = QueryFetchStatus.Fetching(),
            isInvalidated = false
        )
        val actual = QueryRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_success() {
        val expected = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching(),
            isInvalidated = false
        )
        val actual = QueryRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_failure() {
        val error = RuntimeException("error")
        val expected = QueryState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching(),
            isInvalidated = false
        )
        val actual = QueryRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    private class TestQueryKey(
        number: Int = 1,
        private val delayTime: Duration = 100.milliseconds
    ) : QueryKey<String> by buildQueryKey(
        id = QueryId("test/query/$number"),
        fetch = {
            delay(delayTime)
            "Hello, Soil!"
        }
    )
}
