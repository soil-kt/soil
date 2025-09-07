// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryState
import soil.query.SwrCache
import soil.query.buildQueryKey
import soil.query.compose.tooling.QueryPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.core.orNone
import soil.query.test.test
import soil.testing.UnitTest
import kotlin.test.Test


@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class QueryComposableTest : UnitTest() {

    @Test
    fun testRememberQuery() = runUiTest { testScope ->
        val key = TestQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test()
        setContent {
            SwrClientProvider(client) {
                val query = rememberQuery(key, config = QueryConfig {
                    mapper = QueryObjectMapper.Default
                    optimizer = QueryRecompositionOptimizer.Enabled
                    strategy = QueryStrategy.Default
                    marker = Marker.None
                })
                when (val reply = query.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
                    is Reply.None -> Unit
                }
            }
        }
        waitForIdle()
        testScope.runCurrent()
        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Hello, Soil!")
    }

    @Test
    fun testRememberQuery_select() = runUiTest { testScope ->
        val key = TestQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key.id) { "Hello, Compose!" }
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
        testScope.runCurrent()
        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("HELLO, COMPOSE!")
    }

    @Test
    fun testRememberQuery_throwError() = runUiTest { testScope ->
        val key = TestQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key.id) { throw RuntimeException("Failed to do something :(") }
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
        waitForIdle()
        testScope.runCurrent()
        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("error")
    }

    @Test
    fun testRememberQuery_combineTwo() = runUiTest { testScope ->
        val key1 = TestQueryKey(number = 1)
        val key2 = TestQueryKey(number = 2)
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key1.id) { "Hello, Compose!" }
            on(key2.id) { "Hello, Soil!" }
        }
        setContent {
            SwrClientProvider(client) {
                val query = rememberQuery(key1 = key1, key2 = key2, transform = { a, b -> a + b })
                when (val reply = query.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
                    is Reply.None -> Unit
                }
            }
        }
        waitForIdle()
        testScope.runCurrent()
        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Hello, Compose!Hello, Soil!")
    }

    @Test
    fun testRememberQuery_combineThree() = runUiTest { testScope ->
        val key1 = TestQueryKey(number = 1)
        val key2 = TestQueryKey(number = 2)
        val key3 = TestQueryKey(number = 3)
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key1.id) { "Hello, Compose!" }
            on(key2.id) { "Hello, Soil!" }
            on(key3.id) { "Hello, Kotlin!" }
        }
        setContent {
            SwrClientProvider(client) {
                val query =
                    rememberQuery(key1 = key1, key2 = key2, key3 = key3, transform = { a, b, c -> a + b + c })
                when (val reply = query.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
                    is Reply.None -> Unit
                }
            }
        }
        waitForIdle()
        testScope.runCurrent()
        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Hello, Compose!Hello, Soil!Hello, Kotlin!")
    }

    @Test
    fun testRememberQuery_combineN() = runUiTest { testScope ->
        val key1 = TestQueryKey(number = 1)
        val key2 = TestQueryKey(number = 2)
        val key3 = TestQueryKey(number = 3)
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key1.id) { "Hello, Compose!" }
            on(key2.id) { "Hello, Soil!" }
            on(key3.id) { "Hello, Kotlin!" }
        }
        setContent {
            SwrClientProvider(client) {
                val query = rememberQuery(listOf(key1, key2, key3), transform = { it.joinToString("|") })
                when (val reply = query.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
                    is Reply.None -> Unit
                }
            }
        }
        waitForIdle()
        testScope.runCurrent()
        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Hello, Compose!|Hello, Soil!|Hello, Kotlin!")
    }

    @Test
    fun testRememberQuery_loadingPreview() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
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

        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Loading")
    }

    @Test
    fun testRememberQuery_successPreview() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
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

        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Hello, Query!")
    }

    @Test
    fun testRememberQuery_loadingErrorPreview() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) { QueryState.failure(RuntimeException("Error")) }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val query = rememberQuery(key = key)) {
                    is QueryLoadingErrorObject -> Text(
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
    fun testRememberQuery_refreshErrorPreview() = runComposeUiTest {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
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

        waitUntilExactlyOneExists(hasTestTag("query"))
        onNodeWithTag("query").assertTextEquals("Hello, Query!")
    }

    @Test
    fun testRememberQueryIf() = runUiTest { testScope ->
        val key = TestQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test()
        setContent {
            SwrClientProvider(client) {
                var enabled by remember { mutableStateOf(false) }
                val query = rememberQueryIf(enabled, keyFactory = { if (it) key else null })
                Column {
                    Button(onClick = { enabled = !enabled }, modifier = Modifier.testTag("toggle")) {
                        Text("Toggle")
                    }
                    when (val reply = query?.reply.orNone()) {
                        is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
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
        onNodeWithTag("query").assertTextEquals("Hello, Soil!")
    }

    @Test
    fun testRememberQueryIf_select() = runUiTest { testScope ->
        val key = TestQueryKey()
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key.id) { "Hello, Compose!" }
        }
        setContent {
            SwrClientProvider(client) {
                var enabled by remember { mutableStateOf(false) }
                val query = rememberQueryIf(
                    value = enabled,
                    keyFactory = { if (it) key else null },
                    select = { it.uppercase() }
                )
                Column {
                    Button(onClick = { enabled = !enabled }, modifier = Modifier.testTag("toggle")) {
                        Text("Toggle")
                    }
                    when (val reply = query?.reply.orNone()) {
                        is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
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
        onNodeWithTag("query").assertTextEquals("HELLO, COMPOSE!")
    }

    @Test
    fun testRememberQueryIf_combineTwo() = runUiTest { testScope ->
        val key1 = TestQueryKey(number = 1)
        val key2 = TestQueryKey(number = 2)
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key1.id) { "Hello, Compose!" }
            on(key2.id) { "Hello, Soil!" }
        }
        setContent {
            SwrClientProvider(client) {
                var enabled by remember { mutableStateOf(false) }
                val query = rememberQueryIf(
                    value = enabled,
                    keyPairFactory = { if (it) key1 to key2 else null },
                    transform = { a, b -> a + b })
                Column {
                    Button(onClick = { enabled = !enabled }, modifier = Modifier.testTag("toggle")) {
                        Text("Toggle")
                    }
                    when (val reply = query?.reply.orNone()) {
                        is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
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
        onNodeWithTag("query").assertTextEquals("Hello, Compose!Hello, Soil!")
    }

    @Test
    fun testRememberQueryIf_combineThree() = runUiTest { testScope ->
        val key1 = TestQueryKey(number = 1)
        val key2 = TestQueryKey(number = 2)
        val key3 = TestQueryKey(number = 3)
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key1.id) { "Hello, Compose!" }
            on(key2.id) { "Hello, Soil!" }
            on(key3.id) { "Hello, Kotlin!" }
        }
        setContent {
            SwrClientProvider(client) {
                var enabled by remember { mutableStateOf(false) }
                val query = rememberQueryIf(
                    value = enabled,
                    keyTripleFactory = { if (it) Triple(key1, key2, key3) else null },
                    transform = { a, b, c -> a + b + c })
                Column {
                    Button(onClick = { enabled = !enabled }, modifier = Modifier.testTag("toggle")) {
                        Text("Toggle")
                    }
                    when (val reply = query?.reply.orNone()) {
                        is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
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
        onNodeWithTag("query").assertTextEquals("Hello, Compose!Hello, Soil!Hello, Kotlin!")
    }

    @Test
    fun testRememberQueryIf_combineN() = runUiTest { testScope ->
        val key1 = TestQueryKey(number = 1)
        val key2 = TestQueryKey(number = 2)
        val key3 = TestQueryKey(number = 3)
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key1.id) { "Hello, Compose!" }
            on(key2.id) { "Hello, Soil!" }
            on(key3.id) { "Hello, Kotlin!" }
        }
        setContent {
            SwrClientProvider(client) {
                var enabled by remember { mutableStateOf(false) }
                val query = rememberQueryIf(
                    value = enabled,
                    keyListFactory = { if (it) listOf(key1, key2, key3) else null },
                    transform = { it.joinToString("|") })
                Column {
                    Button(onClick = { enabled = !enabled }, modifier = Modifier.testTag("toggle")) {
                        Text("Toggle")
                    }
                    when (val reply = query?.reply.orNone()) {
                        is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("query"))
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
        onNodeWithTag("query").assertTextEquals("Hello, Compose!|Hello, Soil!|Hello, Kotlin!")
    }

    private class TestQueryKey(
        number: Int = 1
    ) : QueryKey<String> by buildQueryKey(
        id = Id(number),
        fetch = { "Hello, Soil!" }
    ) {
        class Id(number: Int) : QueryId<String>("test/query/$number")
    }
}
