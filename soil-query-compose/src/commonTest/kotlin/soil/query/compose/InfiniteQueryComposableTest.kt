// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertAll
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import androidx.compose.ui.test.waitUntilNodeCount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.QueryChunk
import soil.query.QueryState
import soil.query.SwrCache
import soil.query.buildChunks
import soil.query.buildInfiniteQueryKey
import soil.query.chunkedData
import soil.query.compose.tooling.QueryPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.core.orNone
import soil.query.test.test
import soil.testing.UnitTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class InfiniteQueryComposableTest : UnitTest() {

    @Test
    fun testRememberInfiniteQuery() = runUiTest { testScope ->
        val key = TestInfiniteQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test()
        setContent {
            SwrClientProvider(client) {
                val query = rememberInfiniteQuery(key, config = InfiniteQueryConfig {
                    mapper = InfiniteQueryObjectMapper.Default
                    optimizer = InfiniteQueryRecompositionOptimizer.Enabled
                    strategy = InfiniteQueryStrategy.Default
                    marker = Marker.None
                })
                when (val reply = query.reply) {
                    is Reply.Some -> {
                        Column {
                            reply.value.forEach { chunk ->
                                Text(
                                    "Size: ${chunk.data.size} - Page: ${chunk.param.page}",
                                    modifier = Modifier.testTag("query")
                                )
                            }
                            Text("HasNext: ${query.loadMoreParam != null}", modifier = Modifier.testTag("loadMore"))
                        }
                    }

                    is Reply.None -> Unit
                }
            }
        }
        waitForIdle()
        testScope.runCurrent()
        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Size: 10 - Page: 0")
        onNodeWithTag("loadMore").assertTextEquals("HasNext: true")
    }

    @Test
    fun testRememberInfiniteQuery_select() = runUiTest { testScope ->
        val key = TestInfiniteQueryKey()
        val client = SwrCache(testScope.newSwrCachePolicy()).test()
        setContent {
            SwrClientProvider(client) {
                val query = rememberInfiniteQuery(key, select = { it.chunkedData })
                when (val reply = query.reply) {
                    is Reply.Some -> {
                        Column {
                            reply.value.forEach { data ->
                                Text(data, modifier = Modifier.testTag("query"))
                            }
                        }
                    }

                    is Reply.None -> Unit
                }
            }
        }
        waitForIdle()
        testScope.runCurrent()
        waitUntilNodeCount(hasTestTag("query"), 10)
        onAllNodes(hasTestTag("query")).assertAll(hasText("Item", substring = true))
    }

    @Test
    fun testRememberInfiniteQuery_loadMore() = runUiTest { testScope ->
        val key = TestInfiniteQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test()
        setContent {
            SwrClientProvider(client) {
                val query = rememberInfiniteQuery(key)
                when (val reply = query.reply) {
                    is Reply.Some -> {
                        Column {
                            reply.value.forEachIndexed { index, chunk ->
                                Text(
                                    "Size: ${chunk.data.size} - Page: ${chunk.param.page}",
                                    modifier = Modifier.testTag("query${index}")
                                )
                            }
                            val scope = rememberCoroutineScope()
                            val handleClick = query.loadMoreParam?.let {
                                { scope.launch { query.loadMore(it) } }
                            }
                            Button(
                                onClick = { handleClick?.invoke() },
                                enabled = handleClick != null,
                                modifier = Modifier.testTag("loadMore")
                            ) {
                                Text("Load More")
                            }
                        }
                    }

                    is Reply.None -> Unit
                }
            }
        }
        waitForIdle()
        testScope.runCurrent()
        waitUntilExactlyOneExists(hasTestTag("query0"))
        onNodeWithTag("query0").assertTextEquals("Size: 10 - Page: 0")
        onNodeWithTag("loadMore").performClick()

        waitForIdle()
        testScope.runCurrent()

        waitUntilExactlyOneExists(hasTestTag("query1"))
        onNodeWithTag("query1").assertTextEquals("Size: 10 - Page: 1")
    }


    @Test
    fun testRememberInfiniteQuery_loadingPreview() = runComposeUiTest {
        val key = TestInfiniteQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) { QueryState.initial() }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (rememberInfiniteQuery(key = key)) {
                    is InfiniteQueryLoadingObject -> Text("Loading", modifier = Modifier.testTag("query"))
                    else -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Loading")
    }

    @Test
    fun testRememberInfiniteQuery_successPreview() = runComposeUiTest {
        val key = TestInfiniteQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) {
                    QueryState.success(buildChunks {
                        add(QueryChunk((0 until 10).map { "Item $it" }, PageParam(0, 10)))
                        add(QueryChunk((10 until 20).map { "Item $it" }, PageParam(1, 10)))
                        add(QueryChunk((20 until 30).map { "Item $it" }, PageParam(2, 10)))
                    })
                }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val query = rememberInfiniteQuery(key = key, select = { it.chunkedData })) {
                    is InfiniteQuerySuccessObject -> {
                        Column {
                            query.data.forEach { data ->
                                Text(data, modifier = Modifier.testTag("query"))
                            }
                        }
                    }

                    else -> Unit
                }
            }
        }

        waitUntilNodeCount(hasTestTag("query"), 30)
        onAllNodes(hasTestTag("query")).assertAll(hasText("Item", substring = true))
    }

    @Test
    fun testRememberInfiniteQuery_loadingErrorPreview() = runComposeUiTest {
        val key = TestInfiniteQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) { QueryState.failure(RuntimeException("Error")) }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val query = rememberInfiniteQuery(key = key)) {
                    is InfiniteQueryLoadingErrorObject -> Text(
                        query.error.message ?: "",
                        modifier = Modifier.testTag("query")
                    )

                    else -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Error")
    }

    @Test
    fun testRememberInfiniteQuery_refreshErrorPreview() = runComposeUiTest {
        val key = TestInfiniteQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) {
                    QueryState.failure(RuntimeException("Refresh Error"), data = buildList {
                        add(QueryChunk((0 until 10).map { "Item $it" }, PageParam(0, 10)))
                    })
                }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val query = rememberInfiniteQuery(key = key)) {
                    is InfiniteQueryRefreshErrorObject -> Text(
                        "ChunkSize: ${query.data.size}",
                        modifier = Modifier.testTag("query")
                    )

                    else -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("ChunkSize: 1")
    }

    @Test
    fun testRememberInfiniteQueryIf() = runUiTest { testScope ->
        val key = TestInfiniteQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test()
        setContent {
            SwrClientProvider(client) {
                var enabled by remember { mutableStateOf(false) }
                val query = rememberInfiniteQueryIf(enabled, keyFactory = { if (it) key else null })
                Column {
                    Button(onClick = { enabled = !enabled }, modifier = Modifier.testTag("toggle")) {
                        Text("Toggle")
                    }
                    when (val reply = query?.reply.orNone()) {
                        is Reply.Some -> {
                            reply.value.forEach { chunk ->
                                Text(
                                    "Size: ${chunk.data.size} - Page: ${chunk.param.page}",
                                    modifier = Modifier.testTag("query")
                                )
                            }
                        }

                        is Reply.None -> Unit
                    }
                }
            }
        }

        onNodeWithTag("query").assertDoesNotExist()
        onNodeWithTag("toggle").performClick()

        waitForIdle()
        testScope.runCurrent()

        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Size: 10 - Page: 0")
    }

    @Test
    fun testRememberInfiniteQueryIf_select() = runUiTest { testScope ->
        val key = TestInfiniteQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test()
        setContent {
            SwrClientProvider(client) {
                var enabled by remember { mutableStateOf(false) }
                val query = rememberInfiniteQueryIf(
                    value = enabled,
                    keyFactory = { if (it) key else null },
                    select = { it.chunkedData })
                Column {
                    Button(onClick = { enabled = !enabled }, modifier = Modifier.testTag("toggle")) {
                        Text("Toggle")
                    }
                    when (val reply = query?.reply.orNone()) {
                        is Reply.Some -> {
                            reply.value.forEach { data ->
                                Text(data, modifier = Modifier.testTag("query"))
                            }
                        }

                        is Reply.None -> Unit
                    }
                }
            }
        }

        onNodeWithTag("query").assertDoesNotExist()
        onNodeWithTag("toggle").performClick()

        waitForIdle()
        testScope.runCurrent()

        waitUntilNodeCount(hasTestTag("query"), 10)
        onAllNodes(hasTestTag("query")).assertAll(hasText("Item", substring = true))
    }


    private class TestInfiniteQueryKey : InfiniteQueryKey<List<String>, PageParam> by buildInfiniteQueryKey(
        id = Id,
        fetch = { param ->
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
    ) {
        object Id : InfiniteQueryId<List<String>, PageParam>("test/infinite-query")
    }

    private data class PageParam(
        val page: Int,
        val size: Int
    )
}
