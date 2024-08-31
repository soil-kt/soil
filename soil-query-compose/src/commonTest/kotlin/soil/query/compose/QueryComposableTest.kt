// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.material.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryState
import soil.query.SwrCache
import soil.query.SwrCacheScope
import soil.query.buildQueryKey
import soil.query.compose.tooling.QueryPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.test.test
import soil.testing.UnitTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class QueryComposableTest : UnitTest() {

    @Test
    fun testRememberQuery() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrCache(coroutineScope = SwrCacheScope())
        setContent {
            SwrClientProvider(client) {
                val query = rememberQuery(key, config = QueryConfig {
                    strategy = QueryCachingStrategy.Default
                    marker = Marker.None
                })
                when (val reply = query.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
                    is Reply.None -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Hello, Soil!")
    }

    @Test
    fun testRememberQuery_select() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrCache(coroutineScope = SwrCacheScope()).test {
            mock(key.id) { "Hello, Soil!" }
        }
        setContent {
            SwrClientProvider(client) {
                val query = rememberQuery(key = key, select = { it.uppercase() })
                when (val reply = query.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
                    is Reply.None -> Unit
                }
            }
        }

        waitForIdle()
        onNodeWithTag("query").assertTextEquals("HELLO, SOIL!")
    }

    @Test
    fun testRememberQuery_throwError() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrCache(coroutineScope = SwrCacheScope()).test {
            mock(key.id) { throw RuntimeException("Failed to do something :(") }
        }
        setContent {
            SwrClientProvider(client) {
                val query = rememberQuery(key)
                when (val reply = query.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
                    is Reply.None -> Unit
                }
                if (query.error != null) {
                    Text("error", modifier = Modifier.testTag("query"))
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("error")
    }

    @Test
    fun testRememberQuery_loadingPreview() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            queryPreview = QueryPreviewClient {
                on(key.id) { QueryState.initial() }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (rememberQuery(key = key)) {
                    is QueryLoadingObject -> Text("Loading", modifier = Modifier.testTag("query"))
                    else -> Unit
                }
            }
        }

        waitForIdle()
        onNodeWithTag("query").assertTextEquals("Loading")
    }

    @Test
    fun testRememberQuery_successPreview() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            queryPreview = QueryPreviewClient {
                on(key.id) { QueryState.success("Hello, Query!") }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val query = rememberQuery(key = key)) {
                    is QuerySuccessObject -> Text(query.data, modifier = Modifier.testTag("query"))
                    else -> Unit
                }
            }
        }

        waitForIdle()
        onNodeWithTag("query").assertTextEquals("Hello, Query!")
    }

    @Test
    fun testRememberQuery_loadingErrorPreview() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            queryPreview = QueryPreviewClient {
                on(key.id) { QueryState.failure(RuntimeException("Error")) }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val query = rememberQuery(key = key)) {
                    is QueryLoadingErrorObject -> Text(query.error.message ?: "", modifier = Modifier.testTag("query"))
                    else -> Unit
                }
            }
        }

        waitForIdle()
        onNodeWithTag("query").assertTextEquals("Error")
    }

    @Test
    fun testRememberQuery_refreshErrorPreview() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            queryPreview = QueryPreviewClient {
                on(key.id) { QueryState.failure(RuntimeException("Refresh Error"), data = "Hello, Query!") }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val query = rememberQuery(key = key)) {
                    is QueryRefreshErrorObject -> Text(query.data, modifier = Modifier.testTag("query"))
                    else -> Unit
                }
            }
        }

        waitForIdle()
        onNodeWithTag("query").assertTextEquals("Hello, Query!")
    }

    private class TestQueryKey : QueryKey<String> by buildQueryKey(
        id = Id,
        fetch = { "Hello, Soil!" }
    ) {
        object Id : QueryId<String>("test/query")
    }
}
