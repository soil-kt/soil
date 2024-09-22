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
     * The default subscription options.
     */
    val defaultSubscriptionOptions: SubscriptionOptions

    /**
     * Gets the [SubscriptionRef] by the specified [SubscriptionKey].
     */
    @ExperimentalSoilQueryApi
    fun <T> getSubscription(
        key: SubscriptionKey<T>,
        marker: Marker = Marker.None
    ): SubscriptionRef<T>
}

typealias SubscriptionContentEquals<T> = (oldData: T, newData: T) -> Boolean
typealias SubscriptionRecoverData<T> = (error: Throwable) -> T
typealias SubscriptionOptionsOverride = (SubscriptionOptions) -> SubscriptionOptions
