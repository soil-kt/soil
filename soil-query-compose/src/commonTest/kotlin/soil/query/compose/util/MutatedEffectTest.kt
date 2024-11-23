// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.util

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.launch
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.SwrCache
import soil.query.SwrCacheScope
import soil.query.buildMutationKey
import soil.query.compose.SwrClientProvider
import soil.query.compose.rememberMutation
import soil.testing.UnitTest
import kotlin.test.Test

@ExperimentalTestApi
class MutatedEffectTest : UnitTest() {

    @Test
    fun testMutatedEffect() = runComposeUiTest {
        val key = TestMutationKey()
        val client = SwrCache(coroutineScope = SwrCacheScope())
        setContent {
            SwrClientProvider(client) {
                val mutation = rememberMutation(key)
                var mutatedCount by remember { mutableIntStateOf(0) }
                val scope = rememberCoroutineScope()
                Column {
                    Button(
                        onClick = { scope.launch { mutation.mutateAsync("foo") } },
                        modifier = Modifier.testTag("mutation")
                    ) {
                        Text("Mutate")
                    }
                    Text(
                        "${mutation.mutatedCount}",
                        modifier = Modifier.testTag("count")
                    )
                    (1..mutatedCount).forEach { result ->
                        Text(
                            "Mutated: $result",
                            modifier = Modifier.testTag("result$result")
                        )
                    }
                }
                MutatedEffect(mutation) {
                    mutatedCount++
                }
            }
        }

        waitForIdle()
        onNodeWithTag("result").assertDoesNotExist()
        onNodeWithTag("mutation").performClick()

        waitUntilExactlyOneExists(hasTestTag("result1"))
        onNodeWithText("Mutated: 1").assertExists()
        onNodeWithTag("count").assertTextEquals("1")

        onNodeWithTag("mutation").performClick()

        waitUntilExactlyOneExists(hasTestTag("result2"))
        onNodeWithText("Mutated: 2").assertExists()
        onNodeWithTag("count").assertTextEquals("2")
    }

    @Test
    fun testMutatedEffect_withKeySelector() = runComposeUiTest {
        val key = TestMutationKey()
        val client = SwrCache(coroutineScope = SwrCacheScope())
        setContent {
            SwrClientProvider(client) {
                val mutation = rememberMutation(key)
                var mutatedCount by remember { mutableIntStateOf(0) }
                val scope = rememberCoroutineScope()
                Column {
                    Button(
                        onClick = { scope.launch { mutation.mutateAsync("foo") } },
                        modifier = Modifier.testTag("mutation")
                    ) {
                        Text("Mutate")
                    }
                    Text(
                        "${mutation.mutatedCount}",
                        modifier = Modifier.testTag("count")
                    )
                    (1..mutatedCount).forEach { result ->
                        Text(
                            "Mutated: $result",
                            modifier = Modifier.testTag("result$result")
                        )
                    }
                }
                MutatedEffect(
                    mutation = mutation,
                    keySelector = { it.data }
                ) {
                    mutatedCount++
                }
            }
        }

        waitForIdle()
        onNodeWithTag("result").assertDoesNotExist()
        onNodeWithTag("mutation").performClick()

        waitUntilExactlyOneExists(hasTestTag("result1"))
        onNodeWithText("Mutated: 1").assertExists()
        onNodeWithTag("count").assertTextEquals("1")

        onNodeWithTag("mutation").performClick()

        waitUntilExactlyOneExists(hasTestTag("count") and hasText("2"))
        // The key is the same as the first mutation result, so the MutatedEffect is not called.
        onNodeWithText("Mutated: 2").assertDoesNotExist()
        onNodeWithTag("count").assertTextEquals("2")
    }


    private class TestMutationKey : MutationKey<String, String> by buildMutationKey(
        id = MutationId("test"),
        mutate = { variable ->
            "Mutated: $variable"
        }
    )
}
