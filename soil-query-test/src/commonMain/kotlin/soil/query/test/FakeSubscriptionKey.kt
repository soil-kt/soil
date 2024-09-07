// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.test

import kotlinx.coroutines.flow.Flow
import soil.query.SubscriptionKey
import soil.query.SubscriptionReceiver

/**
 * Creates a fake subscription key that returns the result of the given [mock] function.
 */
class FakeSubscriptionKey<T>(
    private val target: SubscriptionKey<T>,
    private val mock: FakeSubscriptionSubscribe<T>
) : SubscriptionKey<T> by target {
    override val subscribe: SubscriptionReceiver.() -> Flow<T> = { mock() }
}

typealias FakeSubscriptionSubscribe<T> = () -> Flow<T>
