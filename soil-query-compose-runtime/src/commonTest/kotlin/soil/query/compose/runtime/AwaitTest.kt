// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.runtime

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.launch
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.SwrCache
import soil.query.SwrCacheScope
import soil.query.buildInfiniteQueryKey
import soil.query.buildQueryKey
import soil.query.compose.SwrClientProvider
import soil.query.compose.rememberInfiniteQuery
import soil.query.compose.rememberQuery
import soil.query.test.test
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.time.Duration

@OptIn(ExperimentalTestApi::class)
class AwaitTest : UnitTest() {

    @Test
    fun testAwait() = runComposeUiTest {
        val deferred = CompletableDeferred<String>()
        val key = TestQueryKey("foo")
        val client = SwrCache(coroutineScope = SwrCacheScope()).test {
            mock(key.id) { deferred.await() }
        }
        setContent {
            SwrClientProvider(client) {
                val query = rememberQuery(key)
                Await(query) { data ->
                    Text(data, modifier = Modifier.testTag("await"))
                }
            }
        }
        waitForIdle()
        onNodeWithTag("await").assertDoesNotExist()

        deferred.complete("Hello, Soil!")
        waitUntilExactlyOneExists(hasTestTag("await"))
        onNodeWithTag("await").assertTextEquals("Hello, Soil!")
    }

    @Test
    fun testAwait_pair() = runComposeUiTest {
        val deferred1 = CompletableDeferred<String>()
        val deferred2 = CompletableDeferred<String>()
        val key1 = TestQueryKey("foo")
        val key2 = TestQueryKey("bar")
        val client = SwrCache(coroutineScope = SwrCacheScope()).test {
            mock(key1.id) { deferred1.await() }
            mock(key2.id) { deferred2.await() }
        }
        setContent {
            SwrClientProvider(client) {
                val query1 = rememberQuery(key1)
                val query2 = rememberQuery(key2)
                Await(query1, query2) { data1, data2 ->
                    Text(data1 + data2, modifier = Modifier.testTag("await"))
                }
            }
        }
        waitForIdle()
        onNodeWithTag("await").assertDoesNotExist()

        deferred1.complete("Hello, Soil!")
        waitForIdle()
        onNodeWithTag("await").assertDoesNotExist()

        deferred2.complete("Hello, Compose!")
        waitUntilExactlyOneExists(hasTestTag("await"))
        onNodeWithTag("await").assertTextEquals("Hello, Soil!Hello, Compose!")
    }

    @Test
    fun testAwait_triple() = runComposeUiTest {
        val deferred1 = CompletableDeferred<String>()
        val deferred2 = CompletableDeferred<String>()
        val deferred3 = CompletableDeferred<Int>()
        val key1 = TestQueryKey("foo")
        val key2 = TestQueryKey("bar")
        val key3 = TestInfiniteQueryKey()
        val client = SwrCache(coroutineScope = SwrCacheScope()).test {
            mock(key1.id) { deferred1.await() }
            mock(key2.id) { deferred2.await() }
            mock(key3.id) { deferred3.await() }
        }
        setContent {
            SwrClientProvider(client) {
                val query1 = rememberQuery(key1)
                val query2 = rememberQuery(key2)
                val query3 = rememberInfiniteQuery(key3, select = { it.first().data })
                Await(query1, query2, query3) { data1, data2, data3 ->
                    Text(data1 + data2 + data3, modifier = Modifier.testTag("await"))
                }
            }
        }
        waitForIdle()
        onNodeWithTag("await").assertDoesNotExist()

        deferred1.complete("Hello, Soil!")
        waitForIdle()
        onNodeWithTag("await").assertDoesNotExist()

        deferred2.complete("Hello, Compose!")
        waitForIdle()
        onNodeWithTag("await").assertDoesNotExist()

        deferred3.complete(3)
        waitUntilExactlyOneExists(hasTestTag("await"))
        onNodeWithTag("await").assertTextEquals("Hello, Soil!Hello, Compose!3")
    }

    @Test
    fun testAwait_withSuspense() = runComposeUiTest {
        val deferred1 = CompletableDeferred<String>()
        val deferred2 = CompletableDeferred<String>()
        val key1 = TestQueryKey("foo")
        val key2 = TestQueryKey("bar")
        val client = SwrCache(coroutineScope = SwrCacheScope()).test {
            mock(key1.id) { deferred1.await() }
            mock(key2.id) { deferred2.await() }
        }
        setContent {
            SwrClientProvider(client) {
                val query1 = rememberQuery(key1)
                val query2 = rememberQuery(key2)
                Suspense(
                    fallback = { Text("Loading...", modifier = Modifier.testTag("fallback")) }
                ) {
                    Await(query1, query2) { data1, data2 ->
                        Text(data1 + data2, modifier = Modifier.testTag("await"))
                    }
                }
            }
        }
        waitForIdle()
        waitUntilExactlyOneExists(hasTestTag("fallback"))
        onNodeWithTag("fallback").assertTextEquals("Loading...")
        onNodeWithTag("await").assertDoesNotExist()

        deferred1.complete("Hello, Soil!")
        waitForIdle()
        onNodeWithTag("fallback").assertTextEquals("Loading...")
        onNodeWithTag("await").assertDoesNotExist()

        deferred2.complete("Hello, Compose!")
        waitUntilExactlyOneExists(hasTestTag("await"))
        onNodeWithTag("await").assertTextEquals("Hello, Soil!Hello, Compose!")
        onNodeWithTag("fallback").assertDoesNotExist()
    }

    @Test
    fun testAwait_withSuspense_refresh() = runComposeUiTest {
        val deferred1 = CompletableDeferred<String>()
        val deferred2 = CompletableDeferred<String>()
        var isFirst = true
        val key = TestQueryKey("foo")
        val client = SwrCache(coroutineScope = SwrCacheScope()).test {
            mock(key.id) {
                if (isFirst) {
                    isFirst = false
                    deferred1.await()
                } else {
                    deferred2.await()
                }
            }
        }
        setContent {
            SwrClientProvider(client) {
                val query = rememberQuery(key)
                Suspense(
                    fallback = { Text("Loading...", modifier = Modifier.testTag("fallback")) },
                    contentThreshold = Duration.ZERO
                ) {
                    Await(query) { data ->
                        Text(data, modifier = Modifier.testTag("await"))
                    }
                }
                val scope = rememberCoroutineScope()
                Button(
                    onClick = { scope.launch { query.refresh() } },
                    modifier = Modifier.testTag("refresh")
                ) {
                    Text("Refresh")
                }
            }
        }
        waitForIdle()
        waitUntilExactlyOneExists(hasTestTag("fallback"))
        onNodeWithTag("fallback").assertTextEquals("Loading...")
        onNodeWithTag("await").assertDoesNotExist()

        deferred1.complete("Hello, Soil!")
        waitForIdle()
        waitUntilExactlyOneExists(hasTestTag("await"))
        onNodeWithTag("await").assertTextEquals("Hello, Soil!")
        onNodeWithTag("fallback").assertDoesNotExist()

        onNodeWithTag("refresh").performClick()
        waitForIdle()
        waitUntilExactlyOneExists(hasTestTag("fallback"))
        onNodeWithTag("fallback").assertTextEquals("Loading...")
        onNodeWithTag("await").assertTextEquals("Hello, Soil!")

        deferred2.complete("Hello, Compose!")
        waitUntilDoesNotExist(hasTestTag("fallback"))
        onNodeWithTag("await").assertTextEquals("Hello, Compose!")
    }

    private class TestQueryKey(val variant: String) : QueryKey<String> by buildQueryKey(
        id = Id(variant),
        fetch = { "Hello, Soil!" }
    ) {
        class Id(variant: String) : QueryId<String>("test/query", "variant" to variant)
    }

    private class TestInfiniteQueryKey : InfiniteQueryKey<Int, Int> by buildInfiniteQueryKey(
        id = Id,
        fetch = { it },
        initialParam = { 0 },
        loadMoreParam = { it.last().param + 1 }
    ) {
        object Id : InfiniteQueryId<Int, Int>("test/infinite-query")
    }
}
