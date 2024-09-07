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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import soil.query.SubscriberStatus
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionState
import soil.query.SwrCachePlus
import soil.query.SwrCacheScope
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.buildSubscriptionKey
import soil.query.compose.tooling.SubscriptionPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import soil.query.core.Marker
import soil.query.core.Reply
import soil.query.test.testPlus
import soil.testing.UnitTest
import kotlin.test.Test

@OptIn(ExperimentalSoilQueryApi::class, ExperimentalTestApi::class)
class SubscriptionComposableTest : UnitTest() {

    @Test
    fun testRememberSubscription() = runComposeUiTest {
        val key = TestSubscriptionKey()
        val client = SwrCachePlus(coroutineScope = SwrCacheScope())
        setContent {
            SwrClientProvider(client) {
                val subscription = rememberSubscription(key, config = SubscriptionConfig {
                    strategy = SubscriptionStrategy.Default
                    marker = Marker.None
                })
                when (val reply = subscription.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("subscription"))
                    is Reply.None -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("Hello, Soil!")
    }

    @Test
    fun testRememberSubscription_select() = runComposeUiTest {
        val key = TestSubscriptionKey()
        val client = SwrCachePlus(coroutineScope = SwrCacheScope()).testPlus {
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

        waitUntilExactlyOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("HELLO, COMPOSE!")
    }

    @Test
    fun testRememberSubscription_throwError() = runComposeUiTest {
        val key = TestSubscriptionKey()
        val client = SwrCachePlus(coroutineScope = SwrCacheScope()).testPlus {
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

        waitUntilExactlyOneExists(hasTestTag("subscription"))
        onNodeWithTag("subscription").assertTextEquals("error")
    }

    @Test
    fun testRememberSubscription_idlePreview() = runComposeUiTest {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) { SubscriptionState.initial(subscriberStatus = SubscriberStatus.NoSubscribers) }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (rememberSubscription(key)) {
                    is SubscriptionIdleObject -> Text("idle", modifier = Modifier.testTag("subscription"))
                    else -> Unit
                }
            }
        }

        waitForIdle()
        onNodeWithTag("subscription").assertTextEquals("idle")
    }

    @Test
    fun testRememberSubscription_loadingPreview() = runComposeUiTest {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) { SubscriptionState.initial(subscriberStatus = SubscriberStatus.Active) }
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

        waitForIdle()
        onNodeWithTag("subscription").assertTextEquals("loading")
    }

    @Test
    fun testRememberSubscription_successPreview() = runComposeUiTest {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) {
                    SubscriptionState.success(
                        data = "Hello, Subscription!",
                        subscriberStatus = SubscriberStatus.Active
                    )
                }
            }
        )
        setContent {
            SwrClientProvider(client) {
                when (val subscription = rememberSubscription(key)) {
                    is SubscriptionSuccessObject -> Text(subscription.data, modifier = Modifier.testTag("subscription"))
                    else -> Unit
                }
            }
        }

        waitForIdle()
        onNodeWithTag("subscription").assertTextEquals("Hello, Subscription!")
    }

    @Test
    fun testRememberSubscription_errorPreview() = runComposeUiTest {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) {
                    SubscriptionState.failure(
                        error = RuntimeException("Error"),
                        subscriberStatus = SubscriberStatus.Active
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

        waitForIdle()
        onNodeWithTag("subscription").assertTextEquals("Error")
    }

    private class TestSubscriptionKey : SubscriptionKey<String> by buildSubscriptionKey(
        id = Id,
        subscribe = { MutableStateFlow("Hello, Soil!") }
    ) {
        object Id : SubscriptionId<String>("test/subscription")
    }
}
