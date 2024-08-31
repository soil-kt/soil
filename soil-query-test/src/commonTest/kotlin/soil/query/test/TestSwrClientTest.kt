// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import soil.query.InfiniteQueryCommands
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.QueryChunks
import soil.query.QueryCommands
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryRef
import soil.query.SwrCache
import soil.query.SwrCachePolicy
import soil.query.buildInfiniteQueryKey
import soil.query.buildMutationKey
import soil.query.buildQueryKey
import soil.query.core.Marker
import soil.query.core.getOrThrow
import soil.testing.UnitTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class TestSwrClientTest : UnitTest() {

    @Test
    fun testMutation() = runTest {
        val client = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = UnconfinedTestDispatcher(testScheduler)
            )
        )
        val testClient = client.test {
            on(ExampleMutationKey.Id) { "Hello, World!" }
        }
        val key = ExampleMutationKey()
        val mutation = testClient.getMutation(key).also { it.launchIn(backgroundScope) }
        mutation.mutate(0)
        assertEquals("Hello, World!", mutation.state.value.reply.getOrThrow())
    }

    @Test
    fun testQuery() = runTest {
        val client = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = UnconfinedTestDispatcher(testScheduler)
            )
        )
        val testClient = client.test {
            on(ExampleQueryKey.Id) { "Hello, World!" }
        }
        val key = ExampleQueryKey()
        val query = testClient.getQuery(key).also { it.launchIn(backgroundScope) }
        query.test()
        assertEquals("Hello, World!", query.state.value.reply.getOrThrow())
    }

    @Test
    fun testInfiniteQuery() = runTest {
        val client = SwrCache(
            policy = SwrCachePolicy(
                coroutineScope = backgroundScope,
                mainDispatcher = UnconfinedTestDispatcher(testScheduler)
            )
        )
        val testClient = client.test {
            on(ExampleInfiniteQueryKey.Id) { "Hello, World!" }
        }
        val key = ExampleInfiniteQueryKey()
        val query = testClient.getInfiniteQuery(key).also { it.launchIn(backgroundScope) }
        query.test()
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

private suspend fun <T> QueryRef<T>.test(): T {
    val deferred = CompletableDeferred<T>()
    send(QueryCommands.Connect(key, marker = Marker.None, callback = deferred::completeWith))
    return deferred.await()
}

private suspend fun <T, S> InfiniteQueryRef<T, S>.test(): QueryChunks<T, S> {
    val deferred = CompletableDeferred<QueryChunks<T, S>>()
    send(InfiniteQueryCommands.Connect(key, marker = Marker.None, callback = deferred::completeWith))
    return deferred.await()
}
