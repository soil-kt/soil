// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import soil.query.core.Actor
import soil.query.core.Marker

/**
 * A reference to an Subscription for [SubscriptionKey].
 *
 * @param T Type of data to receive.
 */
interface SubscriptionRef<T> : Actor {

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

    /**
     * Cancels the Subscription.
     */
    fun cancel()
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
    subscription: Subscription<T>
): SubscriptionRef<T> {
    return SubscriptionRefImpl(key, marker, subscription)
}

private class SubscriptionRefImpl<T>(
    private val key: SubscriptionKey<T>,
    private val marker: Marker,
    private val subscription: Subscription<T>
) : SubscriptionRef<T> {

    private var job: Job? = null

    override val id: SubscriptionId<T>
        get() = key.id

    override val state: StateFlow<SubscriptionState<T>>
        get() = subscription.state

    override fun launchIn(scope: CoroutineScope): Job {
        return subscription.launchIn(scope)
    }

    override suspend fun resume() {
        if (job?.isActive == true) return
        coroutineScope {
            job = launch {
                subscription.source.collect(::receive)
            }
        }
    }

    override fun cancel() {
        job?.cancel()
        job = null
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
