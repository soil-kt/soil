// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Interface for receiving events of window visibility.

 */
interface WindowVisibility {

    /**
     * Adds an [observer] to receive events.
     */
    fun addObserver(observer: Observer)

    /**
     * Removes an [observer] that receives events.
     */
    fun removeObserver(observer: Observer)

    /**
     * Provides a Flow to receive events of window visibility.
     */
    fun asFlow(): Flow<WindowVisibilityEvent> = callbackFlow {
        val observer = object : Observer {
            override fun onReceive(event: WindowVisibilityEvent) {
                trySend(event)
            }
        }
        addObserver(observer)
        awaitClose { removeObserver(observer) }
    }

    /**
     * Observer interface for receiving events of window visibility.
     */
    interface Observer {

        /**
         * Receives a [event] of window visibility.
         */
        fun onReceive(event: WindowVisibilityEvent)
    }

    /**
     * An object indicating unsupported for the capability of window visibility.
     */
    companion object Unsupported : WindowVisibility {
        override fun addObserver(observer: Observer) = Unit

        override fun removeObserver(observer: Observer) = Unit
    }
}

/**
 * Events of window visibility.
 */
enum class WindowVisibilityEvent {

    /**
     * The window enters the foreground.
     */
    Foreground,

    /**
     * The window enters the background.
     */
    Background
}
