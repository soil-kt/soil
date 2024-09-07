// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import kotlinx.coroutines.flow.MutableStateFlow
import soil.query.SubscriberStatus
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionState
import soil.query.SubscriptionStatus
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.buildSubscriptionKey
import soil.query.compose.tooling.SubscriptionPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import soil.query.core.getOrThrow
import soil.query.core.isNone
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalSoilQueryApi::class)
class SubscriptionObjectMapperTest : UnitTest() {

    @Test
    fun testToObject_idle() {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) { SubscriptionState.initial() }
            }
        )
        val subscription = client.getSubscription(key)
        val actual = with(SubscriptionObjectMapper.Default) {
            subscription.state.value.toObject(subscription = subscription, select = { it })
        }
        assertTrue(actual is SubscriptionIdleObject)
        assertTrue(actual.reply.isNone)
        assertEquals(0, actual.replyUpdatedAt)
        assertNull(actual.error)
        assertEquals(0, actual.errorUpdatedAt)
        assertEquals(subscription::resume, actual.subscribe)
        assertEquals(subscription::cancel, actual.unsubscribe)
        assertEquals(subscription::reset, actual.reset)
        assertEquals(SubscriptionStatus.Pending, actual.status)
        assertEquals(SubscriberStatus.NoSubscribers, actual.subscriberStatus)
        assertNull(actual.data)
    }

    @Test
    fun testToObject_loading() {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) { SubscriptionState.initial(subscriberStatus = SubscriberStatus.Active) }
            }
        )
        val subscription = client.getSubscription(key)
        val actual = with(SubscriptionObjectMapper.Default) {
            subscription.state.value.toObject(subscription = subscription, select = { it })
        }
        assertTrue(actual is SubscriptionLoadingObject)
        assertTrue(actual.reply.isNone)
        assertEquals(0, actual.replyUpdatedAt)
        assertNull(actual.error)
        assertEquals(0, actual.errorUpdatedAt)
        assertEquals(subscription::resume, actual.subscribe)
        assertEquals(subscription::cancel, actual.unsubscribe)
        assertEquals(subscription::reset, actual.reset)
        assertEquals(SubscriptionStatus.Pending, actual.status)
        assertEquals(SubscriberStatus.Active, actual.subscriberStatus)
        assertNull(actual.data)
    }

    @Test
    fun testToObject_success() {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) {
                    SubscriptionState.success(
                        data = "Hello, Subscription!",
                        dataUpdatedAt = 400,
                        subscriberStatus = SubscriberStatus.Active
                    )
                }
            }
        )
        val subscription = client.getSubscription(key)
        val actual = with(SubscriptionObjectMapper.Default) {
            subscription.state.value.toObject(subscription = subscription, select = { it })
        }
        assertTrue(actual is SubscriptionSuccessObject)
        assertEquals("Hello, Subscription!", actual.reply.getOrThrow())
        assertEquals(400, actual.replyUpdatedAt)
        assertNull(actual.error)
        assertEquals(0, actual.errorUpdatedAt)
        assertEquals(subscription::resume, actual.subscribe)
        assertEquals(subscription::cancel, actual.unsubscribe)
        assertEquals(subscription::reset, actual.reset)
        assertEquals(SubscriptionStatus.Success, actual.status)
        assertEquals(SubscriberStatus.Active, actual.subscriberStatus)
        assertNotNull(actual.data)
    }

    @Test
    fun testToObject_error() {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) {
                    SubscriptionState.failure(
                        error = RuntimeException("Error"),
                        errorUpdatedAt = 500,
                        subscriberStatus = SubscriberStatus.Active
                    )
                }
            }
        )
        val subscription = client.getSubscription(key)
        val actual = with(SubscriptionObjectMapper.Default) {
            subscription.state.value.toObject(subscription = subscription, select = { it })
        }
        assertTrue(actual is SubscriptionErrorObject)
        assertTrue(actual.reply.isNone)
        assertEquals(0, actual.replyUpdatedAt)
        assertNotNull(actual.error)
        assertEquals(500, actual.errorUpdatedAt)
        assertEquals(subscription::resume, actual.subscribe)
        assertEquals(subscription::cancel, actual.unsubscribe)
        assertEquals(subscription::reset, actual.reset)
        assertEquals(SubscriptionStatus.Failure, actual.status)
        assertEquals(SubscriberStatus.Active, actual.subscriberStatus)
        assertNull(actual.data)
    }

    @Test
    fun testToObject_errorWithData() {
        val key = TestSubscriptionKey()
        val client = SwrPreviewClient(
            subscription = SubscriptionPreviewClient {
                on(key.id) {
                    SubscriptionState.failure(
                        error = RuntimeException("Error"),
                        errorUpdatedAt = 500,
                        data = "Hello, Subscription!",
                        dataUpdatedAt = 400,
                        subscriberStatus = SubscriberStatus.Active
                    )
                }
            }
        )
        val subscription = client.getSubscription(key)
        val actual = with(SubscriptionObjectMapper.Default) {
            subscription.state.value.toObject(subscription = subscription, select = { it })
        }
        assertTrue(actual is SubscriptionErrorObject)
        assertEquals("Hello, Subscription!", actual.reply.getOrThrow())
        assertEquals(400, actual.replyUpdatedAt)
        assertNotNull(actual.error)
        assertEquals(500, actual.errorUpdatedAt)
        assertEquals(subscription::resume, actual.subscribe)
        assertEquals(subscription::cancel, actual.unsubscribe)
        assertEquals(subscription::reset, actual.reset)
        assertEquals(SubscriptionStatus.Failure, actual.status)
        assertEquals(SubscriberStatus.Active, actual.subscriberStatus)
        assertNotNull(actual.data)
    }

    private class TestSubscriptionKey : SubscriptionKey<String> by buildSubscriptionKey(
        id = Id,
        subscribe = { MutableStateFlow("Hello, Soil!") }
    ) {
        object Id : SubscriptionId<String>("test/subscription")
    }
}
