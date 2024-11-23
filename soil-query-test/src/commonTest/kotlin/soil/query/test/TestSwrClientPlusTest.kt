// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionOptions
import soil.query.SwrCachePlus
import soil.query.SwrCachePlusPolicy
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.buildSubscriptionKey
import soil.query.core.getOrThrow
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalSoilQueryApi::class, ExperimentalCoroutinesApi::class)
class TestSwrClientPlusTest : UnitTest() {

    @Test
    fun testSubscription() = runTest {
        val client = SwrCachePlus(
            policy = SwrCachePlusPolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = UnconfinedTestDispatcher(testScheduler),
                subscriptionOptions = SubscriptionOptions(
                    logger = { println(it) }
                )
            )
        )
        val testClient = client.testPlus {
            on(ExampleSubscriptionKey.Id) { MutableStateFlow("Hello, World!") }
        }
        val key = ExampleSubscriptionKey()
        val subscription = testClient.getSubscription(key).also { it.launchIn(backgroundScope) }
        val job = launch { subscription.resume() }
        launch { subscription.state.filter { it.isSuccess }.first() }
        runCurrent()
        assertEquals("Hello, World!", subscription.state.value.reply.getOrThrow())
        job.cancel()
    }
}

private class ExampleSubscriptionKey : SubscriptionKey<String> by buildSubscriptionKey(
    id = Id,
    subscribe = { error("Not implemented") }
) {
    object Id : SubscriptionId<String>(
        namespace = "subscription/example"
    )
}
