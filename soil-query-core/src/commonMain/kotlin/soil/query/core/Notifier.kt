// Copyright 2025 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

/**
 * Interface for objects that can notify about events.
 *
 * This interface represents the producer side of the observer pattern,
 * providing the ability to send events to registered listeners.
 *
 * @param E The type of event that can be notified
 */
interface Notifier<E> {
    /**
     * Notifies all registered listeners about an [event].
     *
     * This method should deliver the event to all currently registered
     * listeners. The implementation should handle any exceptions thrown
     * by individual listeners to prevent one listener from affecting others.
     *
     * @param event The event to send to all listeners
     */
    fun notify(event: E)
}

/**
 * Abstract implementation of [Notifier] that also implements [Listenable].
 *
 * This class provides a complete implementation of the observer pattern,
 * combining both the ability to register/unregister listeners (from [Listenable])
 * and the ability to notify them of events (from [Notifier]).
 *
 * The implementation maintains a set of listeners and ensures safe event
 * delivery even if individual listeners throw exceptions.
 *
 * @param E The type of event this notifier handles
 */
abstract class AbstractNotifier<E> : Notifier<E>, Listenable<E> {
    private val listeners = mutableSetOf<Listener<E>>()

    /**
     * Checks if there are any registered listeners.
     *
     * @return true if at least one listener is registered, false otherwise
     */
    fun hasListeners(): Boolean = listeners.isNotEmpty()

    override fun addListener(listener: Listener<E>) {
        listeners += listener
    }

    override fun removeListener(listener: Listener<E>) {
        listeners -= listener
    }

    override fun notify(event: E) {
        val snapshot = listeners.toList()
        snapshot.forEach { runCatching { it.onEvent(event) } }
    }
}
