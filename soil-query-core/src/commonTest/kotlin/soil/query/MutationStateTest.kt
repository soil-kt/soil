// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MutationStateTest : UnitTest() {

    @Test
    fun testOmit() {
        val state = MutationState(
            reply = Reply(1),
            replyUpdatedAt = 300,
            error = null,
            errorUpdatedAt = 400,
            status = MutationStatus.Success,
            mutatedCount = 1
        )
        val actual = state.omit(
            keys = setOf(
                MutationState.OmitKey.replyUpdatedAt,
                MutationState.OmitKey.errorUpdatedAt,
                MutationState.OmitKey.mutatedCount
            )
        )
        val expected = MutationState(
            reply = Reply(1),
            replyUpdatedAt = 0,
            error = null,
            errorUpdatedAt = 0,
            status = MutationStatus.Success,
            mutatedCount = 0
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_empty() {
        val expectedState = MutationState.success(1, dataUpdatedAt = 300, mutatedCount = 1)
        val actualState = expectedState.omit(emptySet())
        assertEquals(expectedState, actualState)
    }
}
