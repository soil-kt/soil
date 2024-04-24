// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface MemoryPressure {
    fun addObserver(observer: Observer)
    fun removeObserver(observer: Observer)

    fun asFlow(): Flow<MemoryPressureLevel> = callbackFlow {
        val observer = object : Observer {
            override fun onReceive(level: MemoryPressureLevel) {
                trySend(level)
            }
        }
        addObserver(observer)
        awaitClose { removeObserver(observer) }
    }

    interface Observer {
        fun onReceive(level: MemoryPressureLevel)
    }

    companion object Unsupported : MemoryPressure {
        override fun addObserver(observer: Observer) = Unit

        override fun removeObserver(observer: Observer) = Unit
    }
}

enum class MemoryPressureLevel {
    Low,
    High,
    Critical
}
