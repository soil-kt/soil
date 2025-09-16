// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.flow.FlowCollector

/**
 * Interface for receiving events of memory pressure.
 */
interface MemoryPressure : Listenable<MemoryPressureLevel> {

    /**
     * An object indicating unsupported for the capability of memory pressure.
     */
    companion object Unsupported : MemoryPressure {
        override fun addListener(listener: Listener<MemoryPressureLevel>) = Unit
        override fun removeListener(listener: Listener<MemoryPressureLevel>) = Unit
    }
}

/**
 * Levels of memory pressure.
 */
enum class MemoryPressureLevel {

    /**
     * Indicates low memory pressure.
     */
    Low,

    /**
     * Indicates moderate memory pressure.
     */
    High
}

/**
 * Abstract base class for implementing platform-specific memory pressure providers.
 *
 * This class provides a common pattern for implementing memory pressure monitoring
 * across different platforms by managing the lifecycle of a platform-specific receiver
 * that monitors memory pressure events.
 */
abstract class MemoryPressureProvider : AbstractNotifier<MemoryPressureLevel>(),
    MemoryPressure {

    private var receiver: Receiver? = null

    override fun addListener(listener: Listener<MemoryPressureLevel>) {
        super.addListener(listener)
        if (receiver == null) {
            receiver = createReceiver()
            receiver?.start()
        }
    }

    override fun removeListener(listener: Listener<MemoryPressureLevel>) {
        super.removeListener(listener)
        if (!hasListeners()) {
            receiver?.stop()
            receiver = null
        }
    }

    /**
     * Creates a platform-specific receiver for monitoring memory pressure.
     *
     * Subclasses must implement this method to provide a receiver that can monitor
     * memory pressure changes on their specific platform (Android, iOS, Web, etc.).
     *
     * @return A receiver instance that can start/stop monitoring memory pressure
     */
    protected abstract fun createReceiver(): Receiver

    /**
     * Interface for platform-specific memory pressure receivers.
     *
     * Implementations should monitor memory pressure events and notify
     * the provider when memory pressure levels change.
     */
    interface Receiver {

        /**
         * Starts monitoring memory pressure events.
         *
         * Should register appropriate platform-specific listeners or observers
         * to detect when memory pressure levels change.
         */
        fun start()

        /**
         * Stops monitoring memory pressure events.
         *
         * Should unregister any platform-specific listeners or observers
         * to clean up resources and prevent memory leaks.
         */
        fun stop()
    }
}

internal suspend fun observeOnMemoryPressure(
    memoryPressure: MemoryPressure,
    collector: FlowCollector<MemoryPressureLevel>
) {
    memoryPressure.asFlow()
        .collect(collector::emit)
}
