// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Interface for objects that support listener-based event observation.
 *
 * This interface provides a mechanism to register and unregister listeners
 * that will be notified when events of type [E] occur. It follows the
 * observer pattern and is designed to be used with event-driven architectures.
 *
 * @param E The type of event that listeners will receive
 */
interface Listenable<E> {
    /**
     * Registers a [listener] to receive events.
     *
     * The listener will be notified whenever an event of type [E] occurs.
     * Multiple listeners can be registered, and they will all receive the same events.
     *
     * @param listener The listener to register for receiving events
     */
    fun addListener(listener: Listener<E>)

    /**
     * Unregisters a [listener] from receiving events.
     *
     * After calling this method, the listener will no longer be notified of events.
     * If the listener was not previously registered, this method should have no effect.
     *
     * @param listener The listener to unregister from receiving events
     */
    fun removeListener(listener: Listener<E>)
}

/**
 * Converts this [Listenable] into a [Flow] that emits events.
 *
 * This extension function creates a cold Flow that will register a listener
 * when collection starts and automatically unregister it when collection stops.
 * The Flow will emit all events received by the listener during the collection period.
 *
 * @return A Flow that emits events of type [E] from this Listenable
 */
fun <E> Listenable<E>.asFlow(): Flow<E> = callbackFlow {
    val fn = Listener<E> { trySend(it) }
    addListener(fn)
    awaitClose { removeListener(fn) }
}
