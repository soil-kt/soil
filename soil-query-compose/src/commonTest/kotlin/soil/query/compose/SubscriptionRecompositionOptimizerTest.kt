// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.SubscriptionState
import soil.query.SubscriptionStatus
import soil.query.core.Reply
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SubscriptionRecompositionOptimizerTest : UnitTest() {

    @Test
    fun testOmit_default_pending() {
        val state = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Pending
        )
        val actual = SubscriptionRecompositionOptimizer.Enabled.omit(state)
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            status = SubscriptionStatus.Pending
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_success() {
        val state = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Success
        )
        val actual = SubscriptionRecompositionOptimizer.Enabled.omit(state)
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            errorUpdatedAt = 0,
            status = SubscriptionStatus.Success
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
            status = SubscriptionStatus.Failure
        )
        val actual = SubscriptionRecompositionOptimizer.Enabled.omit(state)
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            error = error,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Failure
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_pending() {
        val expected = SubscriptionState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            errorUpdatedAt = 200,
            status = SubscriptionStatus.Pending
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
            status = SubscriptionStatus.Success
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
            status = SubscriptionStatus.Failure
        )
        val actual = SubscriptionRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }
}
