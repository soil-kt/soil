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
import soil.query.InfiniteQueryTestTag
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.MutationRef
import soil.query.MutationTestTag
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryRef
import soil.query.QueryTestTag
import soil.query.SwrCache
import soil.query.SwrClient
import soil.query.core.Marker
import soil.query.core.TestTag
import soil.query.core.UniqueId
import soil.query.marker.TestTagMarker
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
     *
     * @param id The mutation ID that identifies this mutation
     * @param mutate A function that mocks the mutation behavior
     */
    fun <T, S> on(id: MutationId<T, S>, mutate: FakeMutationMutate<T, S>)

    /**
     * Mocks the mutation process corresponding to [MutationTestTag].
     *
     * @param testTag The test tag that identifies this mutation
     * @param mutate A function that mocks the mutation behavior
     */
    fun <T, S> on(testTag: MutationTestTag<T, S>, mutate: FakeMutationMutate<T, S>)

    /**
     * Mocks the query process corresponding to [QueryId].
     *
     * @param id The query ID that identifies this query
     * @param fetch A function that mocks the query behavior
     */
    fun <T> on(id: QueryId<T>, fetch: FakeQueryFetch<T>)

    /**
     * Mocks the query process corresponding to [QueryTestTag].
     *
     * @param testTag The test tag that identifies this query
     * @param fetch A function that mocks the query behavior
     */
    fun <T> on(testTag: QueryTestTag<T>, fetch: FakeQueryFetch<T>)

    /**
     * Mocks the query process corresponding to [InfiniteQueryId].
     *
     * @param id The infinite query ID that identifies this infinite query
     * @param fetch A function that mocks the infinite query behavior
     */
    fun <T, S> on(id: InfiniteQueryId<T, S>, fetch: FakeInfiniteQueryFetch<T, S>)

    /**
     * Mocks the query process corresponding to [InfiniteQueryTestTag].
     *
     * @param testTag The test tag that identifies this infinite query
     * @param fetch A function that mocks the infinite query behavior
     */
    fun <T, S> on(testTag: InfiniteQueryTestTag<T, S>, fetch: FakeInfiniteQueryFetch<T, S>)

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
 *
 * @param initializer A lambda with [TestSwrClient] receiver that initializes mocks
 * @return A test client that can be used to mock queries and mutations
 */
fun SwrCache.test(initializer: TestSwrClient.() -> Unit = {}): TestSwrClient {
    return TestSwrClientImpl(this).apply(initializer)
}

internal class TestSwrClientImpl(
    private val cache: SwrCache
) : TestSwrClient, SwrClient by cache {

    private val mockMutations = mutableMapOf<UniqueId, FakeMutationMutate<*, *>>()
    private val mockQueries = mutableMapOf<UniqueId, FakeQueryFetch<*>>()
    private val mockInfiniteQueries = mutableMapOf<UniqueId, FakeInfiniteQueryFetch<*, *>>()

    private val mockMutationsByTag = mutableMapOf<TestTag, FakeMutationMutate<*, *>>()
    private val mockQueriesByTag = mutableMapOf<TestTag, FakeQueryFetch<*>>()
    private val mockInfiniteQueriesByTag = mutableMapOf<TestTag, FakeInfiniteQueryFetch<*, *>>()

    override fun <T, S> on(id: MutationId<T, S>, mutate: FakeMutationMutate<T, S>) {
        mockMutations[id] = mutate
    }

    override fun <T, S> on(testTag: MutationTestTag<T, S>, mutate: FakeMutationMutate<T, S>) {
        mockMutationsByTag[testTag] = mutate
    }

    override fun <T> on(id: QueryId<T>, fetch: FakeQueryFetch<T>) {
        mockQueries[id] = fetch
    }

    override fun <T> on(testTag: QueryTestTag<T>, fetch: FakeQueryFetch<T>) {
        mockQueriesByTag[testTag] = fetch
    }

    override fun <T, S> on(id: InfiniteQueryId<T, S>, fetch: FakeInfiniteQueryFetch<T, S>) {
        mockInfiniteQueries[id] = fetch
    }

    override fun <T, S> on(testTag: InfiniteQueryTestTag<T, S>, fetch: FakeInfiniteQueryFetch<T, S>) {
        mockInfiniteQueriesByTag[testTag] = fetch
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
        val idMock = mockMutations[key.id] as? FakeMutationMutate<T, S>
        if (idMock != null) {
            return cache.getMutation(FakeMutationKey(key, idMock), marker)
        }
        val testTag = marker[TestTagMarker.Key]?.value
        val testTagMock = testTag?.let { mockMutationsByTag[it] } as? FakeMutationMutate<T, S>
        if (testTagMock != null) {
            return cache.getMutation(FakeMutationKey(key, testTagMock), marker)
        }
        return cache.getMutation(key, marker)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getQuery(
        key: QueryKey<T>,
        marker: Marker
    ): QueryRef<T> {
        val idMock = mockQueries[key.id] as? FakeQueryFetch<T>
        if (idMock != null) {
            return cache.getQuery(FakeQueryKey(key, idMock), marker)
        }
        val testTag = marker[TestTagMarker.Key]?.value
        val testTagMock = testTag?.let { mockQueriesByTag[it] } as? FakeQueryFetch<T>
        if (testTagMock != null) {
            return cache.getQuery(FakeQueryKey(key, testTagMock), marker)
        }
        return cache.getQuery(key, marker)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getInfiniteQuery(
        key: InfiniteQueryKey<T, S>,
        marker: Marker
    ): InfiniteQueryRef<T, S> {
        val idMock = mockInfiniteQueries[key.id] as? FakeInfiniteQueryFetch<T, S>
        if (idMock != null) {
            return cache.getInfiniteQuery(FakeInfiniteQueryKey(key, idMock), marker)
        }
        val testTag = marker[TestTagMarker.Key]?.value
        val testTagMock = testTag?.let { mockInfiniteQueriesByTag[it] } as? FakeInfiniteQueryFetch<T, S>
        if (testTagMock != null) {
            return cache.getInfiniteQuery(FakeInfiniteQueryKey(key, testTagMock), marker)
        }
        return cache.getInfiniteQuery(key, marker)
    }
}
