// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.core.Marker

/**
 * A Subscription client, which allows you to make subscriptions actor and handle [SubscriptionKey].
 */
interface SubscriptionClient {

    /**
     * **Note:** This property is exposed for limited use cases where you may need to call [SubscriptionKey.subscribe] manually.
     * It can be useful as an escape hatch or for synchronous invocations within the data layer.
     */
    val subscriptionReceiver: SubscriptionReceiver

    /**
     * Gets the [SubscriptionRef] by the specified [SubscriptionKey].
     */
    @ExperimentalSoilQueryApi
    fun <T> getSubscription(
        key: SubscriptionKey<T>,
        marker: Marker = Marker.None
    ): SubscriptionRef<T>
}

/**
 * Interface for directly accessing retrieved [Subscription] data by [SubscriptionClient].
 *
 * [SubscriptionInitialData] is designed to calculate initial data from other [Subscription].
 * This is useful when the type included in the list on the overview screen matches the type of content on the detailed screen.
 */
interface SubscriptionReadonlyClient {

    /**
     * Retrieves data of the [SubscriptionKey] associated with the [id].
     */
    fun <T> getSubscriptionData(id: SubscriptionId<T>): T?
}

typealias SubscriptionInitialData<T> = SubscriptionReadonlyClient.() -> T?
typealias SubscriptionPreloadData<T> = suspend SubscriptionReceiver.() -> T?
typealias SubscriptionContentEquals<T> = (oldData: T, newData: T) -> Boolean
typealias SubscriptionContentCacheable<T> = (currentData: T) -> Boolean
typealias SubscriptionRecoverData<T> = (error: Throwable) -> T
typealias SubscriptionOptionsOverride = (SubscriptionOptions) -> SubscriptionOptions
