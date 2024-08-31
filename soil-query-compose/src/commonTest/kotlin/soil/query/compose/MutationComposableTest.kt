// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

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
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.launch
import soil.query.MutationKey
import soil.query.MutationState
import soil.query.SwrCache
import soil.query.SwrCacheScope
import soil.query.buildMutationKey
import soil.query.compose.tooling.MutationPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.test.test
import soil.testing.UnitTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class MutationComposableTest : UnitTest() {

    @Test
    fun testRememberMutation() = runComposeUiTest {
        val key = TestMutationKey()
        val client = SwrCache(coroutineScope = SwrCacheScope())
        setContent {
            SwrClientProvider(client) {
                val mutation = rememberMutation(key, config = MutationConfig {
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

        // TODO: I don't know why but it's broken.
        //  Related issue: https://github.com/JetBrains/compose-multiplatform-core/blob/46232e6533a71625f7599c206594fca5e2e28e09/compose/ui/ui-test/src/skikoMain/kotlin/androidx/compose/ui/test/Assertions.skikoMain.kt#L26
        // onNodeWithTag("result").assertIsNotDisplayed()
        onNodeWithTag("mutation").performClick()
        waitUntilExactlyOneExists(hasTestTag("result"))
        onNodeWithTag("result").assertTextEquals("Soil - 1")
    }

    @Test
    fun testRememberMutation_throwError() = runComposeUiTest {
        val key = TestMutationKey()
        val client = SwrCache(coroutineScope = SwrCacheScope()).test {
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
    fun testRememberMutation_throwErrorAsync() = runComposeUiTest {
        val key = TestMutationKey()
        val client = SwrCache(coroutineScope = SwrCacheScope()).test {
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
    fun testRememberMutation_idlePreview() = runComposeUiTest {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutationPreview = MutationPreviewClient {
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

        waitForIdle()
        onNodeWithTag("mutation").assertTextEquals("Idle")
    }

    @Test
    fun testRememberMutation_loadingPreview() = runComposeUiTest {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutationPreview = MutationPreviewClient {
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

        waitForIdle()
        onNodeWithTag("mutation").assertTextEquals("Loading")
    }

    @Test
    fun testRememberMutation_successPreview() = runComposeUiTest {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutationPreview = MutationPreviewClient {
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

        waitForIdle()
        onNodeWithTag("mutation").assertTextEquals("Hello, Mutation!")
    }

    @Test
    fun testRememberQuery_errorPreview() = runComposeUiTest {
        val key = TestMutationKey()
        val client = SwrPreviewClient(
            mutationPreview = MutationPreviewClient {
                on(key.id) { MutationState.failure(RuntimeException("Error")) }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val query = rememberMutation(key = key)) {
                    is MutationErrorObject -> Text(query.error.message ?: "", modifier = Modifier.testTag("mutation"))
                    else -> Unit
                }
            }
        }

        waitForIdle()
        onNodeWithTag("mutation").assertTextEquals("Error")
    }

    private class TestMutationKey : MutationKey<String, TestForm> by buildMutationKey(
        mutate = { form ->
            "${form.name} - ${form.age}"
        }
    )

    private data class TestForm(
        val name: String,
        val age: Int
    )
}
