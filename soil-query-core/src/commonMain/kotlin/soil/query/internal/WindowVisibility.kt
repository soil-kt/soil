// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface WindowVisibility {

    fun addObserver(observer: Observer)
    fun removeObserver(observer: Observer)

    fun asFlow(): Flow<WindowVisibilityEvent> = callbackFlow {
        val observer = object : Observer {
            override fun onReceive(event: WindowVisibilityEvent) {
                trySend(event)
            }
        }
        addObserver(observer)
        awaitClose { removeObserver(observer) }
    }

    interface Observer {
        fun onReceive(event: WindowVisibilityEvent)
    }

    companion object Unsupported : WindowVisibility {
        override fun addObserver(observer: Observer) = Unit

        override fun removeObserver(observer: Observer) = Unit
    }
}

enum class WindowVisibilityEvent {
    Foreground,
    Background
}
