// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * Functional interface for receiving events from a [Listenable] source.
 *
 * This interface represents a callback that will be invoked when an event occurs.
 * It uses Kotlin's functional interface feature, allowing it to be implemented
 * using lambda expressions for concise event handling.
 *
 * @param E The type of event this listener can handle
 */
fun interface Listener<E> {
    /**
     * Called when an event occurs.
     *
     * This method will be invoked by the event source whenever an event
     * of type [E] needs to be delivered to this listener.
     *
     * @param event The event that occurred
     */
    fun onEvent(event: E)
}
