// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.QueryFetchStatus
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryState
import soil.query.QueryStatus
import soil.query.buildQueryKey
import soil.query.compose.tooling.QueryPreviewClient
import soil.query.compose.tooling.SwrPreviewClient
import soil.query.core.getOrThrow
import soil.query.core.isNone
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QueryObjectMapperTest : UnitTest() {

    @Test
    fun testToObject_loading() {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) { QueryState.initial() }
            }
        )
        val query = client.getQuery(key)
        val actual = with(QueryObjectMapper.Default) {
            query.state.value.toObject(query = query, select = { it })
        }
        assertTrue(actual is QueryLoadingObject)
        assertTrue(actual.reply.isNone)
        assertEquals(0, actual.replyUpdatedAt)
        assertNull(actual.error)
        assertEquals(0, actual.errorUpdatedAt)
        assertEquals(0, actual.staleAt)
        assertEquals(QueryFetchStatus.Idle, actual.fetchStatus)
        assertFalse(actual.isInvalidated)
        assertEquals(query::invalidate, actual.refresh)
        assertEquals(QueryStatus.Pending, actual.status)
        assertNull(actual.data)
    }

    @Test
    fun testToObject_success() {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) {
                    QueryState.success(
                        data = "Hello, Soil!",
                        dataUpdatedAt = 400,
                        dataStaleAt = 500
                    )
                }
            }
        )
        val query = client.getQuery(key)
        val actual = with(QueryObjectMapper.Default) {
            query.state.value.toObject(query = query, select = { it })
        }
        assertTrue(actual is QuerySuccessObject)
        assertEquals("Hello, Soil!", actual.reply.getOrThrow())
        assertEquals(400, actual.replyUpdatedAt)
        assertNull(actual.error)
        assertEquals(0, actual.errorUpdatedAt)
        assertEquals(500, actual.staleAt)
        assertEquals(QueryFetchStatus.Idle, actual.fetchStatus)
        assertFalse(actual.isInvalidated)
        assertEquals(query::invalidate, actual.refresh)
        assertEquals(QueryStatus.Success, actual.status)
        assertNotNull(actual.data)
    }

    @Test
    fun testToObject_loadingError() {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) {
                    QueryState.failure(
                        error = IllegalStateException("Test"),
                        errorUpdatedAt = 300
                    )
                }
            }
        )
        val query = client.getQuery(key)
        val actual = with(QueryObjectMapper.Default) {
            query.state.value.toObject(query = query, select = { it })
        }
        assertTrue(actual is QueryLoadingErrorObject)
        assertTrue(actual.reply.isNone)
        assertEquals(0, actual.replyUpdatedAt)
        assertNotNull(actual.error)
        assertEquals(300, actual.errorUpdatedAt)
        assertEquals(0, actual.staleAt)
        assertEquals(QueryFetchStatus.Idle, actual.fetchStatus)
        assertFalse(actual.isInvalidated)
        assertEquals(query::invalidate, actual.refresh)
        assertEquals(QueryStatus.Failure, actual.status)
        assertNull(actual.data)
    }

    @Test
    fun testToObject_refreshError() {
        val key = TestQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) {
                    QueryState.failure(
                        error = IllegalStateException("Test"),
                        errorUpdatedAt = 600,
                        data = "Hello, Soil!",
                        dataUpdatedAt = 400,
                        dataStaleAt = 500
                    )
                }
            }
        )
        val query = client.getQuery(key)
        val actual = with(QueryObjectMapper.Default) {
            query.state.value.toObject(query = query, select = { it })
        }
        assertTrue(actual is QueryRefreshErrorObject)
        assertEquals("Hello, Soil!", actual.reply.getOrThrow())
        assertEquals(400, actual.replyUpdatedAt)
        assertNotNull(actual.error)
        assertEquals(600, actual.errorUpdatedAt)
        assertEquals(500, actual.staleAt)
        assertEquals(QueryFetchStatus.Idle, actual.fetchStatus)
        assertFalse(actual.isInvalidated)
        assertEquals(query::invalidate, actual.refresh)
        assertEquals(QueryStatus.Failure, actual.status)
        assertNotNull(actual.data)
    }

    private class TestQueryKey : QueryKey<String> by buildQueryKey(
        id = Id,
        fetch = { "Hello, Soil!" }
    ) {
        object Id : QueryId<String>("test/query")
    }
}
