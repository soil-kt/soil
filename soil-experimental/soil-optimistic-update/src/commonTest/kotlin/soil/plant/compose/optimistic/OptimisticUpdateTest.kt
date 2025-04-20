// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.plant.compose.optimistic

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
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import soil.testing.UnitTest
import kotlin.test.Test

@ExperimentalTestApi
class OptimisticUpdateTest : UnitTest() {

    @Test
    fun testOptimisticUpdate() = runComposeUiTest {
        val deferred1 = CompletableDeferred<Unit>()
        var job: Job? = null

        setContent {
            var counter by remember { mutableStateOf(0) }
            val (optimisticState, addOptimistic) = rememberOptimistic(counter)
            val scope = rememberCoroutineScope()

            Column {
                Text(
                    "Counter: $optimisticState",
                    modifier = Modifier.testTag("counter")
                )
                Button(
                    onClick = {
                        job = scope.launch {
                            addOptimistic(counter + 1)
                            deferred1.await()
                            counter += 1
                        }
                    },
                    modifier = Modifier.testTag("increment")
                ) {
                    Text("Increment")
                }
            }
        }

        onNodeWithTag("counter").assertTextEquals("Counter: 0")
        onNodeWithTag("increment").performClick()

        waitForIdle()
        onNodeWithTag("counter").assertTextEquals("Counter: 1")

        deferred1.complete(Unit)
        waitUntil { job?.isActive != true }
        waitForIdle()
        onNodeWithTag("counter").assertTextEquals("Counter: 1")
    }

    @Test
    fun testOptimisticUpdate_withCustomMapper() = runComposeUiTest {
        val deferred1 = CompletableDeferred<Unit>()
        var job: Job? = null

        setContent {
            var list by remember { mutableStateOf(listOf("A", "B", "C")) }
            val (optimisticState, addOptimistic) = rememberOptimistic<List<String>, String>(
                state = list,
                updateFn = { currentList, newItem -> currentList + newItem }
            )
            val scope = rememberCoroutineScope()

            Column {
                Text(
                    "List: ${optimisticState.joinToString()}",
                    modifier = Modifier.testTag("list")
                )
                Button(
                    onClick = {
                        job = scope.launch {
                            addOptimistic("D")
                            deferred1.await()
                            list = list + "D"
                        }
                    },
                    modifier = Modifier.testTag("add")
                ) {
                    Text("Add Item")
                }
            }
        }

        onNodeWithTag("list").assertTextEquals("List: A, B, C")
        onNodeWithTag("add").performClick()

        waitForIdle()
        onNodeWithTag("list").assertTextEquals("List: A, B, C, D")

        deferred1.complete(Unit)
        waitUntil { job?.isActive != true }
        waitForIdle()
        onNodeWithTag("list").assertTextEquals("List: A, B, C, D")
    }

    @Test
    fun testOptimisticUpdate_withExplicitCompletion() = runComposeUiTest {
        val deferred1 = CompletableDeferred<Unit>()
        val deferred2 = CompletableDeferred<Unit>()
        var job: Job? = null

        setContent {
            var counter by remember { mutableStateOf(0) }
            val (optimisticState, addOptimistic) = rememberOptimistic(counter)
            val scope = rememberCoroutineScope()

            Column {
                Text(
                    "Counter: $optimisticState",
                    modifier = Modifier.testTag("counter")
                )
                Button(
                    onClick = {
                        job = scope.launch {
                            val completion = addOptimistic(counter + 1)
                            deferred1.await()
                            counter += 1
                            completion(null)
                            deferred2.await()
                        }
                    },
                    modifier = Modifier.testTag("increment")
                ) {
                    Text("Increment")
                }
            }
        }

        onNodeWithTag("counter").assertTextEquals("Counter: 0")
        onNodeWithTag("increment").performClick()

        waitForIdle()
        onNodeWithTag("counter").assertTextEquals("Counter: 1")

        deferred1.complete(Unit)
        waitForIdle()
        onNodeWithTag("counter").assertTextEquals("Counter: 1")

        deferred2.complete(Unit)
        waitUntil { job?.isActive != true }
        onNodeWithTag("counter").assertTextEquals("Counter: 1")
    }

    @Test
    fun testOptimisticUpdate_withErrorRollback() = runComposeUiTest {
        val deferred1 = CompletableDeferred<Boolean>()
        var job: Job? = null

        setContent {
            var counter by remember { mutableStateOf(0) }
            val (optimisticState, addOptimistic) = rememberOptimistic(counter)
            val scope = rememberCoroutineScope()

            Column {
                Text(
                    "Counter: $optimisticState",
                    modifier = Modifier.testTag("counter")
                )
                Button(
                    onClick = {
                        job = scope.launch {
                            addOptimistic(counter + 1)
                            try {
                                deferred1.await()
                                counter += 1
                            } catch (e: Exception) {
                                cancel("Failed to increment", e)
                            }
                        }
                    },
                    modifier = Modifier.testTag("increment")
                ) {
                    Text("Increment")
                }
            }
        }

        onNodeWithTag("counter").assertTextEquals("Counter: 0")
        onNodeWithTag("increment").performClick()

        waitForIdle()
        onNodeWithTag("counter").assertTextEquals("Counter: 1")

        deferred1.completeExceptionally(IllegalStateException("Simulated error"))
        waitUntil { job?.isActive != true }
        waitForIdle()
        onNodeWithTag("counter").assertTextEquals("Counter: 0")
    }
}
