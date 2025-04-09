// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

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
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionRef
import soil.query.SubscriptionTestTag
import soil.query.SwrCachePlus
import soil.query.SwrClientPlus
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.core.Marker
import soil.query.core.TestTag
import soil.query.core.UniqueId
import soil.query.marker.TestTagMarker

/**
 * This extended interface of the [SwrClientPlus] provides the capability to mock specific queries, mutations, and subscriptions for the purpose of testing.
 * By registering certain keys as mocks, you can control the behavior of these specific keys while the rest of the keys function normally.
 * This allows for more targeted and precise testing of your application.
 *
 * ```kotlin
 * val cache = SwrCachePlus(..)
 * val testClient = cache.test {
 *      on(MySubscriptionId) { MutableStateFlow("returned fake data") }
 * }
 *
 * testClient.doSomething()
 * ```
 */
interface TestSwrClientPlus : TestSwrClient, SwrClientPlus {

    /**
     * Mocks the subscription process corresponding to [SubscriptionId].
     *
     * @param id The subscription ID that identifies this subscription
     * @param subscribe A function that mocks the subscription behavior
     */
    fun <T> on(id: SubscriptionId<T>, subscribe: FakeSubscriptionSubscribe<T>)

    /**
     * Mocks the subscription process corresponding to the given test tag.
     *
     * @param testTag The test tag that identifies this subscription
     * @param subscribe A function that mocks the subscription behavior
     */
    fun <T> on(testTag: SubscriptionTestTag<T>, subscribe: FakeSubscriptionSubscribe<T>)
}

/**
 * Switches [SwrCachePlus] to a test interface.
 *
 * @param initializer A lambda with [TestSwrClientPlus] receiver that initializes mocks
 * @return A test client that can be used to mock queries, mutations, and subscriptions
 */
@OptIn(ExperimentalSoilQueryApi::class)
fun SwrCachePlus.test(initializer: TestSwrClientPlus.() -> Unit = {}): TestSwrClientPlus {
    return TestSwrClientPlusImpl(this).apply(initializer)
}

@OptIn(ExperimentalSoilQueryApi::class)
internal class TestSwrClientPlusImpl(
    private val cache: SwrCachePlus
) : TestSwrClientPlus, SwrClientPlus by cache {

    private val mockMutations = mutableMapOf<UniqueId, FakeMutationMutate<*, *>>()
    private val mockQueries = mutableMapOf<UniqueId, FakeQueryFetch<*>>()
    private val mockInfiniteQueries = mutableMapOf<UniqueId, FakeInfiniteQueryFetch<*, *>>()
    private val mockSubscriptions = mutableMapOf<UniqueId, FakeSubscriptionSubscribe<*>>()

    private val mockMutationsByTag = mutableMapOf<TestTag, FakeMutationMutate<*, *>>()
    private val mockQueriesByTag = mutableMapOf<TestTag, FakeQueryFetch<*>>()
    private val mockInfiniteQueriesByTag = mutableMapOf<TestTag, FakeInfiniteQueryFetch<*, *>>()
    private val mockSubscriptionsByTag = mutableMapOf<TestTag, FakeSubscriptionSubscribe<*>>()

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

    override fun <T> on(id: SubscriptionId<T>, subscribe: FakeSubscriptionSubscribe<T>) {
        mockSubscriptions[id] = subscribe
    }

    override fun <T> on(testTag: SubscriptionTestTag<T>, subscribe: FakeSubscriptionSubscribe<T>) {
        mockSubscriptionsByTag[testTag] = subscribe
    }


    override fun isIdleNow(): Boolean {
        if (cache.mutationStoreView.values.any { it.state.value.isAwaited() }) {
            return false
        }
        if (cache.queryStoreView.values.any { it.state.value.isAwaited() }) {
            return false
        }
        if (cache.subscriptionStoreView.values.any { it.state.value.isAwaited() }) {
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
        if (mock != null) {
            return cache.getMutation(FakeMutationKey(key, mock), marker)
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
        val mock = mockQueries[key.id] as? FakeQueryFetch<T>
        if (mock != null) {
            return cache.getQuery(FakeQueryKey(key, mock), marker)
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
        val mock = mockInfiniteQueries[key.id] as? FakeInfiniteQueryFetch<T, S>
        if (mock != null) {
            return cache.getInfiniteQuery(FakeInfiniteQueryKey(key, mock), marker)
        }
        val testTag = marker[TestTagMarker.Key]?.value
        val testTagMock = testTag?.let { mockInfiniteQueriesByTag[it] } as? FakeInfiniteQueryFetch<T, S>
        if (testTagMock != null) {
            return cache.getInfiniteQuery(FakeInfiniteQueryKey(key, testTagMock), marker)
        }
        return cache.getInfiniteQuery(key, marker)
    }

    @ExperimentalSoilQueryApi
    @Suppress("UNCHECKED_CAST")
    override fun <T> getSubscription(key: SubscriptionKey<T>, marker: Marker): SubscriptionRef<T> {
        val mock = mockSubscriptions[key.id] as? FakeSubscriptionSubscribe<T>
        if (mock != null) {
            return cache.getSubscription(FakeSubscriptionKey(key, mock), marker)
        }
        val testTag = marker[TestTagMarker.Key]?.value
        val testTagMock = testTag?.let { mockSubscriptionsByTag[it] } as? FakeSubscriptionSubscribe<T>
        if (testTagMock != null) {
            return cache.getSubscription(FakeSubscriptionKey(key, testTagMock), marker)
        }
        return cache.getSubscription(key, marker)
    }
}
