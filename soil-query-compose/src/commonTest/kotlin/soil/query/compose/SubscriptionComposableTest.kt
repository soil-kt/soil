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
import androidx.compose.ui.test.waitUntilAtLeastOneExists
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionState
import soil.query.SwrCachePlus
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.buildSubscriptionKey
import soil.query.compose.tooling.SubscriptionPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.core.orNone
import soil.query.test.test
import soil.testing.UnitTest
import kotlin.test.Test

@OptIn(ExperimentalSoilQueryApi::class, ExperimentalTestApi::class)
class SubscriptionComposableTest : UnitTest() {

    @Test
    fun testRememberSubscription() = runUiTest {
        val key = TestSubscriptionKey()
        val client = SwrCachePlus(coroutineScope = it).test()
        setContent {
            SwrClientProvider(client) {
                val subscription = rememberSubscription(key, config = SubscriptionConfig {
                    mapper = SubscriptionObjectMapper.Default
                    optimizer = SubscriptionRecompositionOptimizer.Enabled
                    strategy = SubscriptionStrategy.Default
                    marker = Marker.None
                })
                when (val reply = subscription.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("subscription"))
                    is Reply.None -> Unit
                }
            }
        }

        waitUntilAtLeastOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("Hello, Soil!")
    }

    @Test
    fun testRememberSubscription_select() = runUiTest {
        val key = TestSubscriptionKey()
        val client = SwrCachePlus(coroutineScope = it).test {
            on(key.id) { MutableStateFlow("Hello, Compose!") }
        }
        setContent {
            SwrClientProvider(client) {
                val subscription = rememberSubscription(key = key, select = { it.uppercase() })
                when (val reply = subscription.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("subscription"))
                    is Reply.None -> Unit
                }
            }
        }

        waitUntilAtLeastOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("HELLO, COMPOSE!")
    }

    @Test
    fun testRememberSubscription_throwError() = runUiTest {
        val key = TestSubscriptionKey()
        val client = SwrCachePlus(coroutineScope = it).test {
            on(key.id) { flow { throw RuntimeException("Failed to do something :(") } }
        }
        setContent {
            SwrClientProvider(client) {
                val subscription = rememberSubscription(key)
                when (val reply = subscription.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("subscription"))
                    is Reply.None -> Unit
                }
                if (subscription.error != null) {
                    Text("error", modifier = Modifier.testTag("subscription"))
                }
            }
        }

        waitUntilAtLeastOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("error")
    }

    @Test
    fun testRememberSubscription_loadingPreview() = runUiTest {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) { SubscriptionState.initial() }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (rememberSubscription(key)) {
                    is SubscriptionLoadingObject -> Text("loading", modifier = Modifier.testTag("subscription"))
                    else -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("loading")
    }

    @Test
    fun testRememberSubscription_successPreview() = runUiTest {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) {
                    SubscriptionState.success(
                        data = "Hello, Subscription!"
                    )
                }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val subscription = rememberSubscription(key)) {
                    is SubscriptionSuccessObject -> Text(
                        subscription.data,
                        modifier = Modifier.testTag("subscription")
                    )

                    else -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("Hello, Subscription!")
    }

    @Test
    fun testRememberSubscription_errorPreview() = runUiTest {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) {
                    SubscriptionState.failure(
                        error = RuntimeException("Error")
                    )
                }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val subscription = rememberSubscription(key)) {
                    is SubscriptionErrorObject -> Text(
                        subscription.error.message ?: "",
                        modifier = Modifier.testTag("subscription")
                    )

                    else -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("Error")
    }

    @Test
    fun testRememberSubscriptionIf() = runUiTest {
        val key = TestSubscriptionKey()
        val client = SwrCachePlus(coroutineScope = it).test()
        setContent {
            SwrClientProvider(client) {
                var enabled by remember { mutableStateOf(false) }
                val subscription = rememberSubscriptionIf(enabled, keyFactory = { if (it) key else null })
                Column {
                    Button(onClick = { enabled = !enabled }, modifier = Modifier.testTag("toggle")) {
                        Text("Toggle")
                    }
                    when (val reply = subscription?.reply.orNone()) {
                        is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("subscription"))
                        is Reply.None -> Unit
                    }
                }
            }
        }

        onNodeWithTag("subscription").assertDoesNotExist()
        onNodeWithTag("toggle").performClick()

        waitUntilExactlyOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("Hello, Soil!")
    }

    @Test
    fun testRememberSubscriptionIf_select() = runUiTest {
        val key = TestSubscriptionKey()
        val client = SwrCachePlus(coroutineScope = it).test {
            on(key.id) { MutableStateFlow("Hello, Compose!") }
        }
        setContent {
            SwrClientProvider(client) {
                var enabled by remember { mutableStateOf(false) }
                val subscription =
                    rememberSubscriptionIf(
                        enabled,
                        keyFactory = { if (it) key else null },
                        select = { it.uppercase() })
                Column {
                    Button(onClick = { enabled = !enabled }, modifier = Modifier.testTag("toggle")) {
                        Text("Toggle")
                    }
                    when (val reply = subscription?.reply.orNone()) {
                        is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("subscription"))
                        is Reply.None -> Unit
                    }
                }
            }
        }

        onNodeWithTag("subscription").assertDoesNotExist()
        onNodeWithTag("toggle").performClick()

        waitUntilExactlyOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("HELLO, COMPOSE!")
    }

    private class TestSubscriptionKey : SubscriptionKey<String> by buildSubscriptionKey(
        id = Id,
        subscribe = { MutableStateFlow("Hello, Soil!") }
    ) {
        object Id : SubscriptionId<String>("test/subscription")
    }
}
