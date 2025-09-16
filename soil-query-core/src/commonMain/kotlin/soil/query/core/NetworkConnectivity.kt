// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.scan
import kotlin.time.Duration

/**
 * Interface for receiving events of network connectivity.
 */
interface NetworkConnectivity : Listenable<NetworkConnectivityEvent> {

    /**
     * An object indicating unsupported for the capability of network connectivity.
     */
    companion object Unsupported : NetworkConnectivity {
        override fun addListener(listener: Listener<NetworkConnectivityEvent>) = Unit
        override fun removeListener(listener: Listener<NetworkConnectivityEvent>) = Unit
    }
}

/**
 * Events of network connectivity.
 */
enum class NetworkConnectivityEvent {

    /**
     * The network is available.
     */
    Available,

    /**
     * The network is lost.
     */
    Lost
}

/**
 * Abstract base class for implementing platform-specific network connectivity providers.
 *
 * This class provides a common pattern for implementing network connectivity monitoring
 * across different platforms by managing the lifecycle of a platform-specific receiver
 * that monitors network state changes.
 */
abstract class NetworkConnectivityProvider : AbstractNotifier<NetworkConnectivityEvent>(),
    NetworkConnectivity {

    private var receiver: Receiver? = null

    override fun addListener(listener: Listener<NetworkConnectivityEvent>) {
        super.addListener(listener)
        if (receiver == null) {
            receiver = createReceiver()
            receiver?.start()
        }
    }

    override fun removeListener(listener: Listener<NetworkConnectivityEvent>) {
        super.removeListener(listener)
        if (!hasListeners()) {
            receiver?.stop()
            receiver = null
        }
    }

    /**
     * Creates a platform-specific receiver for monitoring network connectivity.
     *
     * Subclasses must implement this method to provide a receiver that can monitor
     * network connectivity changes on their specific platform (Android, iOS, Web, etc.).
     *
     * @return A receiver instance that can start/stop monitoring network connectivity
     */
    protected abstract fun createReceiver(): Receiver

    /**
     * Interface for platform-specific network connectivity receivers.
     *
     * Implementations should monitor network connectivity changes and notify
     * the provider when the network state changes.
     */
    interface Receiver {

        /**
         * Starts monitoring network connectivity changes.
         *
         * Should register appropriate platform-specific listeners or observers
         * to detect when network connectivity is gained or lost.
         */
        fun start()

        /**
         * Stops monitoring network connectivity changes.
         *
         * Should unregister any platform-specific listeners or observers
         * to clean up resources and prevent memory leaks.
         */
        fun stop()
    }
}

internal suspend fun observeOnNetworkReconnect(
    networkConnectivity: NetworkConnectivity,
    networkResumeAfterDelay: Duration,
    collector: FlowCollector<Unit>
) {
    networkConnectivity.asFlow()
        .distinctUntilChanged()
        .scan(NetworkConnectivityEvent.Available to NetworkConnectivityEvent.Available) { acc, state -> state to acc.first }
        .filter { it.first == NetworkConnectivityEvent.Available && it.second == NetworkConnectivityEvent.Lost }
        .onEach { delay(networkResumeAfterDelay) }
        .collect { collector.emit(Unit) }
}
