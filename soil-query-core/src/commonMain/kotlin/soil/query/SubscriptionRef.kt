// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.onStart
import soil.query.core.InstanceId
import soil.query.core.Marker
import soil.query.core.uuid

/**
 * A reference to an Subscription for [SubscriptionKey].
 *
 * @param T Type of data to receive.
 */
interface SubscriptionRef<T> : AutoCloseable {

    /**
     * A unique identifier used for managing [SubscriptionKey].
     */
    val id: SubscriptionId<T>

    /**
     * [State Flow][StateFlow] to receive the current state of the subscription.
     */
    val state: StateFlow<SubscriptionState<T>>

    /**
     * Resets the Subscription.
     */
    suspend fun reset()

    /**
     * Resumes the Subscription.
     */
    suspend fun resume()
}

/**
 * Creates a new [SubscriptionRef] instance.
 *
 * @param key The [SubscriptionKey] for the Subscription.
 * @param marker The Marker specified in [SubscriptionClient.getSubscription].
 * @param subscription The Subscription to create a reference.
 */
fun <T> SubscriptionRef(
    key: SubscriptionKey<T>,
    marker: Marker,
    subscription: Subscription<T>,
    iid: InstanceId = uuid()
): SubscriptionRef<T> {
    return SubscriptionRefImpl(key, marker, subscription, iid)
}

private class SubscriptionRefImpl<T>(
    private val key: SubscriptionKey<T>,
    private val marker: Marker,
    private val subscription: Subscription<T>,
    private val iid: InstanceId
) : SubscriptionRef<T> {

    init {
        subscription.attach(iid)
    }

    override val id: SubscriptionId<T>
        get() = key.id

    override val state: StateFlow<SubscriptionState<T>>
        get() = subscription.state

    override fun close() {
        subscription.detach(iid)
    }

    override suspend fun resume() {
        subscription.source
            .onStart {
                if (state.value.isFailure) {
                    reset()
                }
            }
            .collect(::receive)
    }

    override suspend fun reset() {
        send(SubscriptionCommands.Reset(key, state.value.revision))
    }

    private suspend fun send(command: SubscriptionCommand<T>) {
        subscription.command.send(command)
    }

    private suspend fun receive(result: Result<T>) {
        send(SubscriptionCommands.Receive(key, result, state.value.revision, marker))
    }
}
