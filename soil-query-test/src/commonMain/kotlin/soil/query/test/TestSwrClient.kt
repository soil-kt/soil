// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.MutationRef
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryRef
import soil.query.SwrCache
import soil.query.SwrClient
import soil.query.core.Marker
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * This extended interface of the [SwrClient] provides the capability to mock specific queries and mutations for the purpose of testing.
 * By registering certain keys as mocks, you can control the behavior of these specific keys while the rest of the keys function normally.
 * This allows for more targeted and precise testing of your application.
 *
 * ```kotlin
 * val cache = SwrCache(..)
 * val testClient = cache.test {
 *      on(MyQueryId) { "returned fake data" }
 * }
 *
 * testClient.doSomething()
 * ```
 */
interface TestSwrClient : SwrClient {

    /**
     * Mocks the mutation process corresponding to [MutationId].
     */
    fun <T, S> on(id: MutationId<T, S>, mutate: FakeMutationMutate<T, S>)

    /**
     * Mocks the query process corresponding to [QueryId].
     */
    fun <T> on(id: QueryId<T>, fetch: FakeQueryFetch<T>)

    /**
     * Mocks the query process corresponding to [InfiniteQueryId].
     */
    fun <T, S> on(id: InfiniteQueryId<T, S>, fetch: FakeInfiniteQueryFetch<T, S>)


    /**
     * Returns true if client is currently idle.
     */
    fun isIdleNow(): Boolean
}

/**
 * Suspends until the [TestSwrClient] is idle.
 */
suspend fun TestSwrClient.awaitIdle(
    context: CoroutineContext = EmptyCoroutineContext,
    timeout: Duration = 1.seconds
) {
    withContext(context) {
        withTimeout(timeout) {
            while (isActive && !isIdleNow()) {
                yield()
            }
        }
    }
}

/**
 * Switches [SwrCache] to a test interface.
 */
fun SwrCache.test(initializer: TestSwrClient.() -> Unit = {}): TestSwrClient {
    return TestSwrClientImpl(this).apply(initializer)
}

internal class TestSwrClientImpl(
    private val cache: SwrCache
) : TestSwrClient, SwrClient by cache {

    private val mockMutations = mutableMapOf<MutationId<*, *>, FakeMutationMutate<*, *>>()
    private val mockQueries = mutableMapOf<QueryId<*>, FakeQueryFetch<*>>()
    private val mockInfiniteQueries = mutableMapOf<InfiniteQueryId<*, *>, FakeInfiniteQueryFetch<*, *>>()

    override fun <T, S> on(id: MutationId<T, S>, mutate: FakeMutationMutate<T, S>) {
        mockMutations[id] = mutate
    }

    override fun <T> on(id: QueryId<T>, fetch: FakeQueryFetch<T>) {
        mockQueries[id] = fetch
    }

    override fun <T, S> on(id: InfiniteQueryId<T, S>, fetch: FakeInfiniteQueryFetch<T, S>) {
        mockInfiniteQueries[id] = fetch
    }

    override fun isIdleNow(): Boolean {
        if (cache.mutationStoreView.values.any { it.state.value.isAwaited() }) {
            return false
        }
        if (cache.queryStoreView.values.any { it.state.value.isAwaited() }) {
            return false
        }
        return true
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getMutation(
        key: MutationKey<T, S>,
        marker: Marker
    ): MutationRef<T, S> {
        val mock = mockMutations[key.id] as? FakeMutationMutate<T, S>
        return if (mock != null) {
            cache.getMutation(FakeMutationKey(key, mock), marker)
        } else {
            cache.getMutation(key, marker)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getQuery(
        key: QueryKey<T>,
        marker: Marker
    ): QueryRef<T> {
        val mock = mockQueries[key.id] as? FakeQueryFetch<T>
        return if (mock != null) {
            cache.getQuery(FakeQueryKey(key, mock), marker)
        } else {
            cache.getQuery(key, marker)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getInfiniteQuery(
        key: InfiniteQueryKey<T, S>,
        marker: Marker
    ): InfiniteQueryRef<T, S> {
        val mock = mockInfiniteQueries[key.id] as? FakeInfiniteQueryFetch<T, S>
        return if (mock != null) {
            cache.getInfiniteQuery(FakeInfiniteQueryKey(key, mock), marker)
        } else {
            cache.getInfiniteQuery(key, marker)
        }
    }
}
