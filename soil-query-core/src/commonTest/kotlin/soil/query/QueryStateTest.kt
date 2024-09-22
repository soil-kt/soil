// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.core.Reply
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class QueryStateTest : UnitTest() {

    @Test
    fun testOmit() {
        val state = QueryState(
            reply = Reply(1),
            replyUpdatedAt = 100,
            error = null,
            errorUpdatedAt = 200,
            staleAt = 300,
            status = QueryStatus.Success,
            fetchStatus = QueryFetchStatus.Fetching,
            isInvalidated = true
        )
        val actual = state.omit(
            keys = setOf(
                QueryState.OmitKey.replyUpdatedAt,
                QueryState.OmitKey.staleAt,
                QueryState.OmitKey.errorUpdatedAt,
                QueryState.OmitKey.fetchStatus
            )
        )
        val expected = QueryState(
            reply = Reply(1),
            error = null,
            status = QueryStatus.Success,
            isInvalidated = true
        )
        assertEquals(expected, actual)
    }

    @Test
    fun testOmit_emptyKeys() {
        val expectedState = QueryState.success(1, dataUpdatedAt = 300, dataStaleAt = 400)
        val actualState = expectedState.omit(emptySet())
        assertEquals(expectedState, actualState)
    }
}
