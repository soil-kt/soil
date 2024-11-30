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
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionRef
import soil.query.SwrCachePlus
import soil.query.SwrClientPlus
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.core.Marker

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
     */
    fun <T> on(id: SubscriptionId<T>, subscribe: FakeSubscriptionSubscribe<T>)
}

/**
 * Switches [SwrCachePlus] to a test interface.
 */
@OptIn(ExperimentalSoilQueryApi::class)
fun SwrCachePlus.test(initializer: TestSwrClientPlus.() -> Unit = {}): TestSwrClientPlus {
    return TestSwrClientPlusImpl(this).apply(initializer)
}

@OptIn(ExperimentalSoilQueryApi::class)
internal class TestSwrClientPlusImpl(
    private val cache: SwrCachePlus
) : TestSwrClientPlus, SwrClientPlus by cache {

    private val mockMutations = mutableMapOf<MutationId<*, *>, FakeMutationMutate<*, *>>()
    private val mockQueries = mutableMapOf<QueryId<*>, FakeQueryFetch<*>>()
    private val mockInfiniteQueries = mutableMapOf<InfiniteQueryId<*, *>, FakeInfiniteQueryFetch<*, *>>()
    private val mockSubscriptions = mutableMapOf<SubscriptionId<*>, FakeSubscriptionSubscribe<*>>()

    override fun <T, S> on(id: MutationId<T, S>, mutate: FakeMutationMutate<T, S>) {
        mockMutations[id] = mutate
    }

    override fun <T> on(id: QueryId<T>, fetch: FakeQueryFetch<T>) {
        mockQueries[id] = fetch
    }

    override fun <T, S> on(id: InfiniteQueryId<T, S>, fetch: FakeInfiniteQueryFetch<T, S>) {
        mockInfiniteQueries[id] = fetch
    }

    override fun <T> on(id: SubscriptionId<T>, subscribe: FakeSubscriptionSubscribe<T>) {
        mockSubscriptions[id] = subscribe
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

    @ExperimentalSoilQueryApi
    @Suppress("UNCHECKED_CAST")
    override fun <T> getSubscription(key: SubscriptionKey<T>, marker: Marker): SubscriptionRef<T> {
        val mock = mockSubscriptions[key.id] as? FakeSubscriptionSubscribe<T>
        return if (mock != null) {
            cache.getSubscription(FakeSubscriptionKey(key, mock), marker)
        } else {
            cache.getSubscription(key, marker)
        }
    }
}
