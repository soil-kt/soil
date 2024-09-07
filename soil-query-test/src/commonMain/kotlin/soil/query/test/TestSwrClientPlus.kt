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
import soil.query.SwrClientPlus
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.core.Marker

/**
 * This extended interface of the [SwrClientPlus] provides the capability to mock specific queries, mutations, and subscriptions for the purpose of testing.
 * By registering certain keys as mocks, you can control the behavior of these specific keys while the rest of the keys function normally.
 * This allows for more targeted and precise testing of your application.
 *
 * ```kotlin
 * val client = SwrCachePlus(..)
 * val testClient = client.testPlus {
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
 * Switches [SwrClientPlus] to a test interface.
 */
fun SwrClientPlus.testPlus(initializer: TestSwrClientPlus.() -> Unit = {}): TestSwrClientPlus {
    return TestSwrClientPlusImpl(this).apply(initializer)
}

internal class TestSwrClientPlusImpl(
    private val target: SwrClientPlus
) : TestSwrClientPlus, SwrClientPlus by target {

    private val testSwrClient = TestSwrClientImpl(target)
    private val mockSubscriptions = mutableMapOf<SubscriptionId<*>, FakeSubscriptionSubscribe<*>>()

    override fun <T, S> on(id: MutationId<T, S>, mutate: FakeMutationMutate<T, S>) {
        testSwrClient.on(id, mutate)
    }

    override fun <T> on(id: QueryId<T>, fetch: FakeQueryFetch<T>) {
        testSwrClient.on(id, fetch)
    }

    override fun <T, S> on(id: InfiniteQueryId<T, S>, fetch: FakeInfiniteQueryFetch<T, S>) {
        testSwrClient.on(id, fetch)
    }

    override fun <T> on(id: SubscriptionId<T>, subscribe: FakeSubscriptionSubscribe<T>) {
        mockSubscriptions[id] = subscribe
    }

    override fun <T, S> getMutation(key: MutationKey<T, S>, marker: Marker): MutationRef<T, S> {
        return testSwrClient.getMutation(key, marker)
    }

    override fun <T> getQuery(key: QueryKey<T>, marker: Marker): QueryRef<T> {
        return testSwrClient.getQuery(key, marker)
    }

    override fun <T, S> getInfiniteQuery(key: InfiniteQueryKey<T, S>, marker: Marker): InfiniteQueryRef<T, S> {
        return testSwrClient.getInfiniteQuery(key, marker)
    }

    @ExperimentalSoilQueryApi
    @Suppress("UNCHECKED_CAST")
    override fun <T> getSubscription(key: SubscriptionKey<T>, marker: Marker): SubscriptionRef<T> {
        val mock = mockSubscriptions[key.id] as? FakeSubscriptionSubscribe<T>
        return if (mock != null) {
            target.getSubscription(FakeSubscriptionKey(key, mock), marker)
        } else {
            target.getSubscription(key, marker)
        }
    }
}
