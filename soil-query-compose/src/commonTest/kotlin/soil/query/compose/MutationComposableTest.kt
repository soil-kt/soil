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
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.launch
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.MutationState
import soil.query.SwrCache
import soil.query.buildMutationKey
import soil.query.compose.tooling.MutationPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.core.orNone
import soil.query.test.test
import soil.testing.UnitTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class MutationComposableTest : UnitTest() {

    @Test
    fun testRememberMutation() = runUiTest {
        val key = TestMutationKey()
        val client = SwrCache(coroutineScope = it).test()
        setContent {
            SwrClientProvider(client) {
                val mutation = rememberMutation(key, config = MutationConfig {
                    mapper = MutationObjectMapper.Default
                    optimizer = MutationRecompositionOptimizer.Enabled
                    strategy = MutationStrategy.Default
                    marker = Marker.None
                })
                when (val reply = mutation.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("result"))
                    is Reply.None -> Unit
                }
                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch {
                            mutation.mutate(TestForm("Soil", 1))
                        }
                    },
                    modifier = Modifier.testTag("mutation")
                ) {
                    Text("Mutate")
                }
            }
        }

        onNodeWithTag("result").assertDoesNotExist()
        onNodeWithTag("mutation").performClick()

        waitUntilExactlyOneExists(hasTestTag("result"))
        onNodeWithTag("result").assertTextEquals("Soil - 1")
    }

    @Test
    fun testRememberMutation_throwError() = runUiTest {
        val key = TestMutationKey()
        val client = SwrCache(coroutineScope = it).test {
            on(key.id) { throw RuntimeException("Failed to do something :(") }
        }
        setContent {
            SwrClientProvider(client) {
                val mutation = rememberMutation(key)
                when (val reply = mutation.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("result"))
                    is Reply.None -> Unit
                }
                if (mutation.error != null) {
                    Text("error", modifier = Modifier.testTag("result"))
                }
                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                mutation.mutate(TestForm("Soil", 1))
                            } catch (e: RuntimeException) {
                                // unused
                            }
                        }
                    },
                    modifier = Modifier.testTag("mutation")
                ) {
                    Text("Mutate")
                }
            }
        }

        onNodeWithTag("mutation").performClick()

        waitUntilExactlyOneExists(hasTestTag("result"))
        onNodeWithTag("result").assertTextEquals("error")
    }

    @Test
    fun testRememberMutation_throwErrorAsync() = runUiTest {
        val key = TestMutationKey()
        val client = SwrCache(coroutineScope = it).test {
            on(key.id) { throw RuntimeException("Failed to do something :(") }
        }
        setContent {
            SwrClientProvider(client) {
                val mutation = rememberMutation(key)
                when (val reply = mutation.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("result"))
                    is Reply.None -> Unit
                }
                if (mutation.error != null) {
                    Text("error", modifier = Modifier.testTag("result"))
                }
                val scope = rememberCoroutineScope()
                Button(
                    onClick = {
                        scope.launch {
                            mutation.mutateAsync(TestForm("Soil", 1))
                        }
                    },
                    modifier = Modifier.testTag("mutation")
                ) {
                    Text("Mutate")
                }
            }
        }

        onNodeWithTag("mutation").performClick()

        waitUntilExactlyOneExists(hasTestTag("result"))
        onNodeWithTag("result").assertTextEquals("error")
    }

    @Test
    fun testRememberMutation_idlePreview() = runUiTest {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutation = MutationPreviewClient {
                on(key.id) { MutationState.initial() }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (rememberMutation(key = key)) {
                    is MutationIdleObject -> Text("Idle", modifier = Modifier.testTag("mutation"))
                    else -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("mutation"))
        onNodeWithTag("mutation").assertTextEquals("Idle")
    }

    @Test
    fun testRememberMutation_loadingPreview() = runUiTest {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutation = MutationPreviewClient {
                on(key.id) { MutationState.pending() }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (rememberMutation(key = key)) {
                    is MutationLoadingObject -> Text("Loading", modifier = Modifier.testTag("mutation"))
                    else -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("mutation"))
        onNodeWithTag("mutation").assertTextEquals("Loading")
    }

    @Test
    fun testRememberMutation_successPreview() = runUiTest {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutation = MutationPreviewClient {
                on(key.id) { MutationState.success("Hello, Mutation!") }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val query = rememberMutation(key = key)) {
                    is MutationSuccessObject -> Text(query.data, modifier = Modifier.testTag("mutation"))
                    else -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("mutation"))
        onNodeWithTag("mutation").assertTextEquals("Hello, Mutation!")
    }

    @Test
    fun testRememberQuery_errorPreview() = runUiTest {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutation = MutationPreviewClient {
                on(key.id) { MutationState.failure(RuntimeException("Error")) }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val query = rememberMutation(key = key)) {
                    is MutationErrorObject -> Text(
                        query.error.message ?: "",
                        modifier = Modifier.testTag("mutation")
                    )

                    else -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("mutation"))
        onNodeWithTag("mutation").assertTextEquals("Error")
    }

    @Test
    fun testRememberMutationIf() = runUiTest {
        val key = TestMutationKey()
        val client = SwrCache(coroutineScope = it).test()
        setContent {
            SwrClientProvider(client) {
                var enabled by remember { mutableStateOf(false) }
                val mutation = rememberMutationIf(enabled, { if (it) key else null })
                Column {
                    Button(onClick = { enabled = !enabled }, modifier = Modifier.testTag("toggle")) {
                        Text("Toggle")
                    }
                    when (val reply = mutation?.reply.orNone()) {
                        is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("result"))
                        is Reply.None -> Unit
                    }
                    val scope = rememberCoroutineScope()
                    if (mutation != null) {
                        Button(
                            onClick = {
                                scope.launch {
                                    mutation.mutate(TestForm("Soil", 1))
                                }
                            },
                            modifier = Modifier.testTag("mutation")
                        ) {
                            Text("Mutate")
                        }
                    }
                }
            }
        }

        onNodeWithTag("mutation").assertDoesNotExist()
        onNodeWithTag("toggle").performClick()

        waitUntilExactlyOneExists(hasTestTag("mutation"))
        onNodeWithTag("result").assertDoesNotExist()
        onNodeWithTag("mutation").performClick()

        waitUntilExactlyOneExists(hasTestTag("result"))
        onNodeWithTag("result").assertTextEquals("Soil - 1")
    }

    private class TestMutationKey : MutationKey<String, TestForm> by buildMutationKey(
        id = Id,
        mutate = { form ->
            "${form.name} - ${form.age}"
        }
    ) {
        object Id : MutationId<String, TestForm>(
            namespace = "test/mutation"
        )
    }

    private data class TestForm(
        val name: String,
        val age: Int
    )
}
