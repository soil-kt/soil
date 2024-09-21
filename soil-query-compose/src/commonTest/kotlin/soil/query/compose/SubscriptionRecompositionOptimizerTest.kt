// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import androidx.compose.material.Text
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.test.waitUntilExactlyOneExists
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import soil.query.SubscriberStatus
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionState
import soil.query.SubscriptionStatus
import soil.query.SwrCachePlus
import soil.query.SwrCacheScope
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.buildSubscriptionKey
import soil.query.core.Reply
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalTestApi::class, ExperimentalSoilQueryApi::class)
class SubscriptionRecompositionOptimizerTest : UnitTest() {

    @Test
    fun testRecomposition_default() = runComposeUiTest {
        val key = TestSubscriptionKey()
        val client = SwrCachePlus(coroutineScope = SwrCacheScope())
        var recompositionCount = 0
        setContent {
            SwrClientProvider(client) {
                val subscription = rememberSubscription(key)
                SideEffect { recompositionCount++ }
                when (val reply = subscription.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("subscription"))
                    is Reply.None -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("subscription"))

        // pending(active) -> success
        assertEquals(2, recompositionCount)
    }

    @Test
    fun testRecomposition_disabled() = runComposeUiTest {
        val key = TestSubscriptionKey()
        val client = SwrCachePlus(coroutineScope = SwrCacheScope())
        var recompositionCount = 0
        setContent {
            SwrClientProvider(client) {
                val subscription = rememberSubscription(key, config = SubscriptionConfig {
                    optimizer = SubscriptionRecompositionOptimizer.Disabled
                })
                SideEffect { recompositionCount++ }
                when (val reply = subscription.reply) {
                    is Reply.Some -> Text(reply.value, modifier = Modifier.testTag("subscription"))
                    is Reply.None -> Unit
                }
            }
        }

        waitUntilExactlyOneExists(hasTestTag("subscription"))

        // pending(no-subscribers) -> pending(active) -> success
        assertEquals(3, recompositionCount)
    }

    @Test
    fun testOmit_default_pending() {
        val state = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Pending,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        val actual = SubscriptionRecompositionOptimizer.Default.omit(state)
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            status = SubscriptionStatus.Pending,
            subscriberStatus = SubscriberStatus.Active
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_success() {
        val state = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Success,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        val actual = SubscriptionRecompositionOptimizer.Default.omit(state)
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            status = SubscriptionStatus.Success,
            subscriberStatus = SubscriberStatus.Active
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_failure() {
        val error = RuntimeException("error")
        val state = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Failure,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        val actual = SubscriptionRecompositionOptimizer.Default.omit(state)
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            error = error,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Failure,
            subscriberStatus = SubscriberStatus.Active
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_lazy_pending() {
        val state = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Pending,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        val actual = SubscriptionRecompositionOptimizer.Lazy.omit(state)
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            status = SubscriptionStatus.Pending,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_lazy_success() {
        val state = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Success,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        val actual = SubscriptionRecompositionOptimizer.Lazy.omit(state)
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            status = SubscriptionStatus.Success,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_lazy_failure() {
        val error = RuntimeException("error")
        val state = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Failure,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        val actual = SubscriptionRecompositionOptimizer.Lazy.omit(state)
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            error = error,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Failure,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_pending() {
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Pending,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        val actual = SubscriptionRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_success() {
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Success,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        val actual = SubscriptionRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_failure() {
        val error = RuntimeException("error")
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Failure,
            subscriberStatus = SubscriberStatus.NoSubscribers
        )
        val actual = SubscriptionRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    private class TestSubscriptionKey(
        private val delayTime: Duration = 100.milliseconds
    ) : SubscriptionKey<String> by buildSubscriptionKey(
        id = SubscriptionId("test/subscription"),
        subscribe = {
            flow {
                delay(delayTime)
                emit("Hello, Soil!")
            }
        }
    )
}
