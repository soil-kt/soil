// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubscriptionStateTest : UnitTest() {

    @Test
    fun testOmit() {
        val state = SubscriptionState(
            reply = Reply(1),
            replyUpdatedAt = 300,
            error = null,
            errorUpdatedAt = 400,
            status = SubscriptionStatus.Success
        )
        val actual = state.omit(
            keys = setOf(
                SubscriptionState.OmitKey.replyUpdatedAt,
                SubscriptionState.OmitKey.errorUpdatedAt
            )
        )
        val expected = SubscriptionState(
            reply = Reply(1),
            replyUpdatedAt = 0,
            error = null,
            errorUpdatedAt = 0,
            status = SubscriptionStatus.Success
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_defaultSubscriberStatus() {
        val state = SubscriptionState(
            reply = Reply(1),
            replyUpdatedAt = 300,
            error = null,
            errorUpdatedAt = 400,
            status = SubscriptionStatus.Success
        )
        val actual = state.omit(
            keys = setOf(
                SubscriptionState.OmitKey.replyUpdatedAt,
                SubscriptionState.OmitKey.errorUpdatedAt
            )
        )
        val expected = SubscriptionState(
            reply = Reply(1),
            replyUpdatedAt = 0,
            error = null,
            errorUpdatedAt = 0,
            status = SubscriptionStatus.Success
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_empty() {
        val expected = SubscriptionState.success(1, dataUpdatedAt = 300)
        val actual = expected.omit(emptySet())
        assertEquals(expected, actual)
    }
}
