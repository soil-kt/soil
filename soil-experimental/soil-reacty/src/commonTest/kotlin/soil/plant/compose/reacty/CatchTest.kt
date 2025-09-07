// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.reacty

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
import androidx.compose.ui.test.waitUntilDoesNotExist
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.SwrCache
import soil.query.buildQueryKey
import soil.query.compose.SwrClientProvider
import soil.query.compose.rememberQuery
import soil.query.test.test
import soil.testing.UnitTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class, ExperimentalCoroutinesApi::class)
class CatchTest : UnitTest() {

    @Test
    fun testCatch() = runUiTest { testScope ->
        val deferred1 = CompletableDeferred<String>()
        val deferred2 = CompletableDeferred<String>()
        var isFirst = true
        val key = TestQueryKey("foo")
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key.id) {
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
                Catch(query) {
                    Text("Error", modifier = Modifier.testTag("catch"))
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
        testScope.runCurrent()
        onNodeWithTag("catch").assertDoesNotExist()

        deferred1.completeExceptionally(RuntimeException("Error"))
        testScope.runCurrent()
        waitUntilExactlyOneExists(hasTestTag("catch"))
        onNodeWithTag("catch").assertTextEquals("Error")

        onNodeWithTag("refresh").performClick()
        deferred2.complete("Hello, Soil!")
        testScope.runCurrent()
        waitUntilDoesNotExist(hasTestTag("catch"))
    }


    @Test
    fun testCatch_withErrorBoundary() = runUiTest { testScope ->
        val deferred1 = CompletableDeferred<String>()
        val deferred2 = CompletableDeferred<String>()
        var isFirst = true
        val key = TestQueryKey("foo")
        val client = SwrCache(policy = testScope.newSwrCachePolicy()).test {
            on(key.id) {
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
                ErrorBoundary(
                    fallback = { Text("Error", modifier = Modifier.testTag("fallback")) }
                ) {
                    Catch(query)
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
        testScope.runCurrent()
        onNodeWithTag("fallback").assertDoesNotExist()

        deferred1.completeExceptionally(RuntimeException("Error"))
        testScope.runCurrent()
        waitUntilExactlyOneExists(hasTestTag("fallback"))
        onNodeWithTag("fallback").assertTextEquals("Error")

        onNodeWithTag("refresh").performClick()
        deferred2.complete("Hello, Soil!")
        testScope.runCurrent()
        waitUntilDoesNotExist(hasTestTag("fallback"))
    }

    private class TestQueryKey(val variant: String) : QueryKey<String> by buildQueryKey(
        id = Id(variant),
        fetch = { "Hello, Soil!" }
    ) {
        class Id(variant: String) : QueryId<String>("test/query", "variant" to variant)
    }
}
