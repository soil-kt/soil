// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryTestTag
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.MutationTestTag
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryTestTag
import soil.query.SwrCache
import soil.query.SwrCachePolicy
import soil.query.buildInfiniteQueryKey
import soil.query.buildMutationKey
import soil.query.buildQueryKey
import soil.query.core.Marker
import soil.query.core.getOrThrow
import soil.query.marker.testTag
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestSwrClientTest : UnitTest() {

    @Test
    fun testMutation_withId() = runTest {
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
        val mutation = testClient.getMutation(key)
        launch { mutation.mutate(0) }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, World!", mutation.state.value.reply.getOrThrow())
        mutation.close()
    }

    @Test
    fun testMutation_withTestTag() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val cache = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = testDispatcher
            )
        )

        val testTag = ExampleMutationKey.TestTag()
        val testClient = cache.test {
            on(testTag) {
                "Hello, TestTag!"
            }
        }
        val key = ExampleMutationKey()
        val mutation = testClient.getMutation(key, Marker.testTag(testTag))
        launch { mutation.mutate(0) }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, TestTag!", mutation.state.value.reply.getOrThrow())
        mutation.close()
    }

    @Test
    fun testQuery_withId() = runTest {
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
        val query = testClient.getQuery(key)
        launch { query.resume() }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, World!", query.state.value.reply.getOrThrow())
        query.close()
    }

    @Test
    fun testQuery_withTestTag() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val cache = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = testDispatcher
            )
        )
        val testTag = ExampleQueryKey.TestTag()
        val testClient = cache.test {
            on(testTag) { "Hello, TestTag!" }
        }
        val key = ExampleQueryKey()
        val query = testClient.getQuery(key, Marker.testTag(testTag))
        launch { query.resume() }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, TestTag!", query.state.value.reply.getOrThrow())
        query.close()
    }

    @Test
    fun testInfiniteQuery_withId() = runTest {
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
        val query = testClient.getInfiniteQuery(key)
        launch { query.resume() }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, World!", query.state.value.reply.getOrThrow().first().data)
        query.close()
    }

    @Test
    fun testInfiniteQuery_withTestTag() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val cache = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = testDispatcher
            )
        )
        val testTag = ExampleInfiniteQueryKey.TestTag()
        val testClient = cache.test {
            on(testTag) { "Hello, TestTag!" }
        }
        val key = ExampleInfiniteQueryKey()
        val query = testClient.getInfiniteQuery(key, Marker.testTag(testTag))
        launch { query.resume() }

        testClient.awaitIdle(testDispatcher)
        assertEquals("Hello, TestTag!", query.state.value.reply.getOrThrow().first().data)
        query.close()
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

    class TestTag : MutationTestTag<String, Int>("mutation/example")
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

    class TestTag : QueryTestTag<String>("query/example")
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

    class TestTag : InfiniteQueryTestTag<String, Int>("infinite-query/example")
}
