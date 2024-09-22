// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.MutationState
import soil.query.MutationStatus
import soil.query.core.Reply
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MutationRecompositionOptimizerTest : UnitTest() {

    @Test
    fun testOmit_default_idle() {
        val state = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = null,
            errorUpdatedAt = 200,
            status = MutationStatus.Idle,
            mutatedCount = 1
        )
        val actual = MutationRecompositionOptimizer.Default.omit(state)
        val expected = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            error = null,
            errorUpdatedAt = 0,
            status = MutationStatus.Idle,
            mutatedCount = 0
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_pending() {
        val state = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = null,
            errorUpdatedAt = 200,
            status = MutationStatus.Pending,
            mutatedCount = 1
        )
        val actual = MutationRecompositionOptimizer.Default.omit(state)
        val expected = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            error = null,
            errorUpdatedAt = 0,
            status = MutationStatus.Pending,
            mutatedCount = 0
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_success() {
        val state = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = null,
            errorUpdatedAt = 200,
            status = MutationStatus.Success,
            mutatedCount = 1
        )
        val actual = MutationRecompositionOptimizer.Default.omit(state)
        val expected = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            error = null,
            errorUpdatedAt = 0,
            status = MutationStatus.Success,
            mutatedCount = 0
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_default_failure() {
        val error = RuntimeException("error")
        val state = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            status = MutationStatus.Failure,
            mutatedCount = 1
        )
        val actual = MutationRecompositionOptimizer.Default.omit(state)
        val expected = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 0,
            error = error,
            errorUpdatedAt = 200,
            status = MutationStatus.Failure,
            mutatedCount = 0
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_idle() {
        val expected = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = null,
            errorUpdatedAt = 200,
            status = MutationStatus.Idle,
            mutatedCount = 1
        )
        val actual = MutationRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_pending() {
        val expected = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = null,
            errorUpdatedAt = 200,
            status = MutationStatus.Pending,
            mutatedCount = 1
        )
        val actual = MutationRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_success() {
        val expected = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = null,
            errorUpdatedAt = 200,
            status = MutationStatus.Success,
            mutatedCount = 1
        )
        val actual = MutationRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_disabled_failure() {
        val error = RuntimeException("error")
        val expected = MutationState.test(
            reply = Reply.some(1),
            replyUpdatedAt = 300,
            error = error,
            errorUpdatedAt = 200,
            status = MutationStatus.Failure,
            mutatedCount = 1
        )
        val actual = MutationRecompositionOptimizer.Disabled.omit(expected)
        assertEquals(expected, actual)
    }
}
