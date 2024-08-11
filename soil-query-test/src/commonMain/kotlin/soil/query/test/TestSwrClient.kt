// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import soil.query.InfiniteQueryId
import soil.query.InfiniteQueryKey
import soil.query.InfiniteQueryRef
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.MutationRef
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryRef
import soil.query.SwrClient

/**
 * This extended interface of the [SwrClient] provides the capability to mock specific queries and mutations for the purpose of testing.
 * By registering certain keys as mocks, you can control the behavior of these specific keys while the rest of the keys function normally.
 * This allows for more targeted and precise testing of your application.
 *
 * ```kotlin
 * val client = SwrCache(..)
 * val testClient = client.test()
 * testClient.mock(MyQueryId) { "returned fake data" }
 *
 * testClient.doSomething()
 * ```
 */
interface TestSwrClient : SwrClient {

    /**
     * Mocks the mutation process corresponding to [MutationId].
     */
    fun <T, S> mock(id: MutationId<T, S>, mutate: FakeMutationMutate<T, S>)

    /**
     * Mocks the query process corresponding to [QueryId].
     */
    fun <T> mock(id: QueryId<T>, fetch: FakeQueryFetch<T>)

    /**
     * Mocks the query process corresponding to [InfiniteQueryId].
     */
    fun <T, S> mock(id: InfiniteQueryId<T, S>, fetch: FakeInfiniteQueryFetch<T, S>)
}

/**
 * Switches [SwrClient] to a test interface.
 */
fun SwrClient.test(): TestSwrClient = TestSwrClientImpl(this)

internal class TestSwrClientImpl(
    private val target: SwrClient
) : TestSwrClient, SwrClient by target {

    private val mockMutations = mutableMapOf<MutationId<*, *>, FakeMutationMutate<*, *>>()
    private val mockQueries = mutableMapOf<QueryId<*>, FakeQueryFetch<*>>()
    private val mockInfiniteQueries = mutableMapOf<InfiniteQueryId<*, *>, FakeInfiniteQueryFetch<*, *>>()

    override fun <T, S> mock(id: MutationId<T, S>, mutate: FakeMutationMutate<T, S>) {
        mockMutations[id] = mutate
    }

    override fun <T> mock(id: QueryId<T>, fetch: FakeQueryFetch<T>) {
        mockQueries[id] = fetch
    }

    override fun <T, S> mock(id: InfiniteQueryId<T, S>, fetch: FakeInfiniteQueryFetch<T, S>) {
        mockInfiniteQueries[id] = fetch
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getMutation(key: MutationKey<T, S>): MutationRef<T, S> {
        val mock = mockMutations[key.id] as? FakeMutationMutate<T, S>
        return if (mock != null) {
            target.getMutation(FakeMutationKey(key, mock))
        } else {
            target.getMutation(key)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getQuery(key: QueryKey<T>): QueryRef<T> {
        val mock = mockQueries[key.id] as? FakeQueryFetch<T>
        return if (mock != null) {
            target.getQuery(FakeQueryKey(key, mock))
        } else {
            target.getQuery(key)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getInfiniteQuery(key: InfiniteQueryKey<T, S>): InfiniteQueryRef<T, S> {
        val mock = mockInfiniteQueries[key.id] as? FakeInfiniteQueryFetch<T, S>
        return if (mock != null) {
            target.getInfiniteQuery(FakeInfiniteQueryKey(key, mock))
        } else {
            target.getInfiniteQuery(key)
        }
    }
}
