// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose

import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.QueryChunk
import soil.query.QueryFetchStatus
import soil.query.QueryState
import soil.query.QueryStatus
import soil.query.buildChunks
import soil.query.buildInfiniteQueryKey
import soil.query.chunkedData
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

class InfiniteQueryObjectMapperTest : UnitTest() {

    @Test
    fun testToObject_loading() {
        val key = TestInfiniteQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) { QueryState.initial() }
            }
        )
        val query = client.getInfiniteQuery(key)
        val actual = with(InfiniteQueryObjectMapper.Default) {
            query.state.value.toObject(query = query, select = { it.chunkedData })
        }
        assertTrue(actual is InfiniteQueryLoadingObject)
        assertTrue(actual.reply.isNone)
        assertEquals(0, actual.replyUpdatedAt)
        assertNull(actual.error)
        assertEquals(0, actual.errorUpdatedAt)
        assertEquals(0, actual.staleAt)
        assertEquals(QueryFetchStatus.Idle, actual.fetchStatus)
        assertFalse(actual.isInvalidated)
        assertEquals(query::invalidate, actual.refresh)
        assertEquals(query::loadMore, actual.loadMore)
        assertNull(actual.loadMoreParam)
        assertEquals(QueryStatus.Pending, actual.status)
        assertNull(actual.data)
    }

    @Test
    fun testToObject_success() {
        val key = TestInfiniteQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) {
                    QueryState.success(buildChunks {
                        add(QueryChunk((0 until 10).map { "Item $it" }, PageParam(0, 10)))
                        add(QueryChunk((10 until 20).map { "Item $it" }, PageParam(1, 10)))
                        add(QueryChunk((20 until 30).map { "Item $it" }, PageParam(2, 10)))
                    }, dataUpdatedAt = 300, dataStaleAt = 400)
                }
            }
        )
        val query = client.getInfiniteQuery(key)
        val actual = with(InfiniteQueryObjectMapper.Default) {
            query.state.value.toObject(query = query, select = { it.chunkedData })
        }
        assertTrue(actual is InfiniteQuerySuccessObject)
        assertEquals(30, actual.reply.getOrThrow().size)
        assertEquals(300, actual.replyUpdatedAt)
        assertNull(actual.error)
        assertEquals(0, actual.errorUpdatedAt)
        assertEquals(400, actual.staleAt)
        assertEquals(QueryFetchStatus.Idle, actual.fetchStatus)
        assertFalse(actual.isInvalidated)
        assertEquals(query::invalidate, actual.refresh)
        assertEquals(query::loadMore, actual.loadMore)
        assertEquals(PageParam(3, 10), actual.loadMoreParam)
        assertEquals(QueryStatus.Success, actual.status)
        assertNotNull(actual.data)
    }

    @Test
    fun testToObject_loadingError() {
        val key = TestInfiniteQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) {
                    QueryState.failure(
                        error = RuntimeException("Error"),
                        errorUpdatedAt = 200
                    )
                }
            }
        )
        val query = client.getInfiniteQuery(key)
        val actual = with(InfiniteQueryObjectMapper.Default) {
            query.state.value.toObject(query = query, select = { it.chunkedData })
        }
        assertTrue(actual is InfiniteQueryLoadingErrorObject)
        assertTrue(actual.reply.isNone)
        assertEquals(0, actual.replyUpdatedAt)
        assertNotNull(actual.error)
        assertEquals(200, actual.errorUpdatedAt)
        assertEquals(0, actual.staleAt)
        assertEquals(QueryFetchStatus.Idle, actual.fetchStatus)
        assertFalse(actual.isInvalidated)
        assertEquals(query::invalidate, actual.refresh)
        assertEquals(query::loadMore, actual.loadMore)
        assertNull(actual.loadMoreParam)
        assertEquals(QueryStatus.Failure, actual.status)
        assertNull(actual.data)
    }

    @Test
    fun testToObject_refreshError() {
        val key = TestInfiniteQueryKey()
        val client = SwrPreviewClient(
            query = QueryPreviewClient {
                on(key.id) {
                    QueryState.failure(
                        error = RuntimeException("Error"),
                        errorUpdatedAt = 600,
                        data = buildList {
                            add(QueryChunk((0 until 10).map { "Item $it" }, PageParam(0, 10)))
                            add(QueryChunk((10 until 20).map { "Item $it" }, PageParam(1, 10)))
                            add(QueryChunk((20 until 30).map { "Item $it" }, PageParam(2, 10)))
                        },
                        dataUpdatedAt = 300,
                        dataStaleAt = 400
                    )
                }
            }
        )
        val query = client.getInfiniteQuery(key)
        val actual = with(InfiniteQueryObjectMapper.Default) {
            query.state.value.toObject(query = query, select = { it.chunkedData })
        }
        assertTrue(actual is InfiniteQueryRefreshErrorObject)
        assertEquals(30, actual.reply.getOrThrow().size)
        assertEquals(300, actual.replyUpdatedAt)
        assertNotNull(actual.error)
        assertEquals(600, actual.errorUpdatedAt)
        assertEquals(400, actual.staleAt)
        assertEquals(QueryFetchStatus.Idle, actual.fetchStatus)
        assertFalse(actual.isInvalidated)
        assertEquals(query::invalidate, actual.refresh)
        assertEquals(query::loadMore, actual.loadMore)
        assertEquals(PageParam(3, 10), actual.loadMoreParam)
        assertEquals(QueryStatus.Failure, actual.status)
        assertNotNull(actual.data)
    }

    private class TestInfiniteQueryKey : InfiniteQueryKey<List<String>, PageParam> by buildInfiniteQueryKey(
        id = Id,
        fetch = { param ->
            val startPosition = param.page * param.size
            (startPosition..<startPosition + param.size).map {
                "Item $it"
            }
        },
        initialParam = { PageParam(0, 10) },
        loadMoreParam = {
            val chunk = it.last()
            PageParam(chunk.param.page + 1, 10)
        }
    ) {
        object Id : InfiniteQueryId<List<String>, PageParam>("test/infinite-query")
    }

    private data class PageParam(
        val page: Int,
        val size: Int
    )
}
