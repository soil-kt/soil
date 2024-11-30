// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import soil.query.core.Actor

/**
 * Subscription as the base interface for an [SubscriptionClient] implementations.
 *
 * @param T Type of the receive value from the subscription.
 */
interface Subscription<T> : Actor {

    /**
     * [Shared Flow][SharedFlow] to receive subscription result.
     */
    val source: SharedFlow<Result<T>>

    /**
     * [Shared Flow][SharedFlow] to receive subscription [events][SubscriptionEvent].
     */
    val event: SharedFlow<SubscriptionEvent>

    /**
     * [State Flow][StateFlow] to receive the current state of the subscription.
     */
    val state: StateFlow<SubscriptionState<T>>

    /**
     * [Send Channel][SendChannel] to manipulate the state of the subscription.
     */
    val command: SendChannel<SubscriptionCommand<T>>
}

/**
 * Events occurring in the subscription.
 */
enum class SubscriptionEvent {
    Resume
}
