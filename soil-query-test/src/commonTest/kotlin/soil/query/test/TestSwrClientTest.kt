// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.SwrCache
import soil.query.SwrCachePolicy
import soil.query.buildInfiniteQueryKey
import soil.query.buildMutationKey
import soil.query.buildQueryKey
import soil.query.core.getOrThrow
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSwrClientTest : UnitTest() {

    @Test
    fun testMutation() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val cache = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = testDispatcher
            )
        )
        val testClient = cache.test {
            on(ExampleMutationKey.Id) {
                "Hello, World!"
            }
        }
        val key = ExampleMutationKey()
        val mutation = testClient.getMutation(key).also { it.launchIn(backgroundScope) }
        launch { mutation.mutate(0) }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, World!", mutation.state.value.reply.getOrThrow())
    }

    @Test
    fun testQuery() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val cache = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = testDispatcher
            )
        )
        val testClient = cache.test {
            on(ExampleQueryKey.Id) { "Hello, World!" }
        }
        val key = ExampleQueryKey()
        val query = testClient.getQuery(key).also { it.launchIn(backgroundScope) }
        launch { query.resume() }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, World!", query.state.value.reply.getOrThrow())
    }

    @Test
    fun testInfiniteQuery() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val cache = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = testDispatcher
            )
        )
        val testClient = cache.test {
            on(ExampleInfiniteQueryKey.Id) { "Hello, World!" }
        }
        val key = ExampleInfiniteQueryKey()
        val query = testClient.getInfiniteQuery(key).also { it.launchIn(backgroundScope) }
        launch { query.resume() }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, World!", query.state.value.reply.getOrThrow().first().data)
    }
}

private class ExampleMutationKey : MutationKey<String, Int> by buildMutationKey(
    id = Id,
    mutate = {
        error("Not implemented")
    }
) {
    object Id : MutationId<String, Int>(
        namespace = "mutation/example"
    )
}

private class ExampleQueryKey : QueryKey<String> by buildQueryKey(
    id = Id,
    fetch = {
        error("Not implemented")
    }
) {
    object Id : QueryId<String>(
        namespace = "query/example"
    )
}

private class ExampleInfiniteQueryKey : InfiniteQueryKey<String, Int> by buildInfiniteQueryKey(
    id = Id,
    fetch = {
        error("Not implemented")
    },
    initialParam = { 0 },
    loadMoreParam = { null }
) {
    object Id : InfiniteQueryId<String, Int>(
        namespace = "infinite-query/example"
    )
}
