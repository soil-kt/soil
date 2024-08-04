// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.core

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Interface for receiving events of memory pressure.
 */
interface MemoryPressure {

    /**
     * Adds an [observer] to receive events.
     */
    fun addObserver(observer: Observer)

    /**
     * Removes an [observer] that receives events.
     */
    fun removeObserver(observer: Observer)

    /**
     * Provides a Flow to receive events of memory pressure.
     */
    fun asFlow(): Flow<MemoryPressureLevel> = callbackFlow {
        val observer = object : Observer {
            override fun onReceive(level: MemoryPressureLevel) {
                trySend(level)
            }
        }
        addObserver(observer)
        awaitClose { removeObserver(observer) }
    }

    /**
     * Observer interface for receiving events of memory pressure.
     */
    interface Observer {

        /**
         * Receives a [level] of memory pressure.
         */
        fun onReceive(level: MemoryPressureLevel)
    }

    /**
     * An object indicating unsupported for the capability of memory pressure.
     */
    companion object Unsupported : MemoryPressure {
        override fun addObserver(observer: Observer) = Unit

        override fun removeObserver(observer: Observer) = Unit
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
