// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.delay
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.QueryChunks
import soil.query.QueryFetchStatus
import soil.query.QueryState
import soil.query.QueryStatus
import soil.query.SwrCache
import soil.query.buildInfiniteQueryKey
import soil.query.core.Reply
import soil.query.emptyChunks
import soil.query.test.test
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalTestApi::class)
class InfiniteQueryRecompositionOptimizerTest : UnitTest() {

    @Test
    fun testRecompositionCount_default() = runUiTest {
        val key = TestInfiniteQueryKey()
        val client = SwrCache(coroutineScope = it).test()
        var recompositionCount = 0
        setContent {
            SwrClientProvider(client) {
                val query = rememberInfiniteQuery(key)
                SideEffect {
                    recompositionCount++
                }
                when (val reply = query.reply) {
                    is Reply.Some -> {
                        Column {
                            reply.value.forEach { chunk ->
                                Text(
                                    "Size: ${chunk.data.size} - Page: ${chunk.param.page}",
                                    modifier = Modifier.testTag("query")
                                )
                            }
                        }
                    }

                    is Reply.None -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("query"))

        // pending -> success
        assertEquals(2, recompositionCount)
    }

    @Test
    fun testRecompositionCount_disabled() = runUiTest {
        val key = TestInfiniteQueryKey()
        val client = SwrCache(coroutineScope = it).test()
        var recompositionCount = 0
        setContent {
            SwrClientProvider(client) {
                val query = rememberInfiniteQuery(key, config = InfiniteQueryConfig {
                    optimizer = InfiniteQueryRecompositionOptimizer.Disabled
                })
                SideEffect { recompositionCount++ }
                when (val reply = query.reply) {
                    is Reply.Some -> {
                        Column {
                            reply.value.forEach { chunk ->
                                Text(
                                    "Size: ${chunk.data.size} - Page: ${chunk.param.page}",
                                    modifier = Modifier.testTag("query")
                                )
                            }
                        }
                    }

                    is Reply.None -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("query"))

        // pending -> pending(fetching) -> success
        assertEquals(3, recompositionCount)
    }

    @Test
    fun testOmit_pending() {
        val state = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.None,
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Pending,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = false
        )
        val actual = InfiniteQueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test<QueryChunks<Int, Int>>(
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
        val state = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.some(emptyChunks()),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = false
        )
        val actual = InfiniteQueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.some(emptyChunks()),
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
        val state = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.some(emptyChunks()),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = true
        )
        val actual = InfiniteQueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.some(emptyChunks()),
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            staleAt = 0,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = true
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_failure() {
        val error = RuntimeException("error")
        val state = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.some(emptyChunks()),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = false
        )
        val actual = InfiniteQueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.some(emptyChunks()),
            replyUpdatedAt = 0,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 0,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Idle,
            isInvalidated = false
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_failure_isInvalidated() {
        val error = RuntimeException("error")
        val state = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.some(emptyChunks()),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = true
        )
        val actual = InfiniteQueryRecompositionOptimizer.Enabled.omit(state)
        val expected = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.some(emptyChunks()),
            replyUpdatedAt = 0,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 0,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = true
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_pending() {
        val expected = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.None,
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Pending,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = false
        )
        val actual = InfiniteQueryRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_success() {
        val expected = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.some(emptyChunks()),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = false
        )
        val actual = InfiniteQueryRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_failure() {
        val error = RuntimeException("error")
        val expected = QueryState.test<QueryChunks<Int, Int>>(
            reply = Reply.some(emptyChunks()),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            staleAt = 400,
            status = QueryStatus.Failure,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = false
        )
        val actual = InfiniteQueryRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    private class TestInfiniteQueryKey(
        private val delayTime: Duration = 100.milliseconds
    ) : InfiniteQueryKey<List<String>, PageParam> by buildInfiniteQueryKey(
        id = InfiniteQueryId("test/infinite-query"),
        fetch = { param ->
            delay(delayTime)
            val startPosition = param.page * param.size
            (startPosition..<startPosition + param.size).map {
                "Item $it"
            }
        },
        initialParam = { PageParam(0, 10) },
        loadMoreParam = {
            val chunk = it.last()
            PageParam(chunk.param.page + 1, 10)
        }
    )

    private data class PageParam(
        val page: Int,
        val size: Int
    )
}
