// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionTestTag
import soil.query.SwrCachePlus
import soil.query.SwrCachePlusPolicy
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.buildSubscriptionKey
import soil.query.core.Marker
import soil.query.core.getOrThrow
import soil.query.marker.testTag
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSoilQueryApi::class)
class TestSwrClientPlusTest : UnitTest() {

    @Test
    fun testSubscription_withId() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val cache = SwrCachePlus(
            policy = SwrCachePlusPolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = testDispatcher
            )
        )
        val testClient = cache.test {
            on(ExampleSubscriptionKey.Id) { MutableStateFlow("Hello, World!") }
        }
        val key = ExampleSubscriptionKey()
        val subscription = testClient.getSubscription(key)
        // Use backgroundScope for auto cancel
        backgroundScope.launch { subscription.resume() }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, World!", subscription.state.value.reply.getOrThrow())
        subscription.close()
    }

    @Test
    fun testSubscription_withTestTag() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val cache = SwrCachePlus(
            policy = SwrCachePlusPolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = testDispatcher
            )
        )
        val testTag = ExampleSubscriptionKey.TestTag()
        val testClient = cache.test {
            on(testTag) { MutableStateFlow("Hello, TestTag!") }
        }
        val key = ExampleSubscriptionKey()
        val subscription = testClient.getSubscription(key, Marker.testTag(testTag))
        // Use backgroundScope for auto cancel
        backgroundScope.launch { subscription.resume() }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, TestTag!", subscription.state.value.reply.getOrThrow())
        subscription.close()
    }
}

private class ExampleSubscriptionKey : SubscriptionKey<String> by buildSubscriptionKey(
    id = Id,
    subscribe = { error("Not implemented") }
) {
    object Id : SubscriptionId<String>(
        namespace = "subscription/example"
    )

    class TestTag : SubscriptionTestTag<String>("subscription/example")
}
