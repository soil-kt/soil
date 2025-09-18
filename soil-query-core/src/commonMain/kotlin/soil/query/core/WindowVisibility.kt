// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.scan

/**
 * Interface for receiving events of window visibility.

 */
interface WindowVisibility : Listenable<WindowVisibilityEvent> {

    /**
     * An object indicating unsupported for the capability of window visibility.
     */
    companion object Unsupported : WindowVisibility {
        override fun addListener(listener: Listener<WindowVisibilityEvent>) = Unit
        override fun removeListener(listener: Listener<WindowVisibilityEvent>) = Unit
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

/**
 * Abstract base class for implementing platform-specific window visibility providers.
 *
 * This class provides a common pattern for implementing window visibility monitoring
 * across different platforms by managing the lifecycle of a platform-specific receiver
 * that monitors window visibility state changes.
 */
abstract class WindowVisibilityProvider : AbstractNotifier<WindowVisibilityEvent>(),
    WindowVisibility {

    private var receiver: Receiver? = null

    override fun addListener(listener: Listener<WindowVisibilityEvent>) {
        super.addListener(listener)
        if (receiver == null) {
            receiver = createReceiver()
            receiver?.start()
        }
    }

    override fun removeListener(listener: Listener<WindowVisibilityEvent>) {
        super.removeListener(listener)
        if (!hasListeners()) {
            receiver?.stop()
            receiver = null
        }
    }

    /**
     * Creates a platform-specific receiver for monitoring window visibility.
     *
     * Subclasses must implement this method to provide a receiver that can monitor
     * window visibility changes on their specific platform (Android, iOS, Web, etc.).
     *
     * @return A receiver instance that can start/stop monitoring window visibility
     */
    protected abstract fun createReceiver(): Receiver

    /**
     * Interface for platform-specific window visibility receivers.
     *
     * Implementations should monitor window visibility state changes and notify
     * the provider when the window transitions between foreground and background states.
     */
    interface Receiver {

        /**
         * Starts monitoring window visibility changes.
         *
         * Should register appropriate platform-specific listeners or observers
         * to detect when the window becomes visible or hidden.
         */
        fun start()

        /**
         * Stops monitoring window visibility changes.
         *
         * Should unregister any platform-specific listeners or observers
         * to clean up resources and prevent memory leaks.
         */
        fun stop()
    }
}


internal suspend fun observeOnWindowFocus(
    windowVisibility: WindowVisibility,
    collector: FlowCollector<Unit>
) {
    windowVisibility.asFlow()
        .distinctUntilChanged()
        .scan(WindowVisibilityEvent.Foreground to WindowVisibilityEvent.Foreground) { acc, state -> state to acc.first }
        .filter { it.first == WindowVisibilityEvent.Foreground && it.second == WindowVisibilityEvent.Background }
        .collect { collector.emit(Unit) }
}
