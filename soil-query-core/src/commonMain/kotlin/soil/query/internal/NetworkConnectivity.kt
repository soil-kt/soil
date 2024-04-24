// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.internal

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface NetworkConnectivity {
    fun addObserver(observer: Observer)
    fun removeObserver(observer: Observer)

    fun asFlow(): Flow<NetworkConnectivityEvent> = callbackFlow {
        val observer = object : Observer {
            override fun onReceive(event: NetworkConnectivityEvent) {
                trySend(event)
            }
        }
        addObserver(observer)
        awaitClose { removeObserver(observer) }
    }

    interface Observer {
        fun onReceive(event: NetworkConnectivityEvent)
    }

    companion object Unsupported : NetworkConnectivity {
        override fun addObserver(observer: Observer) = Unit

        override fun removeObserver(observer: Observer) = Unit
    }
}

enum class NetworkConnectivityEvent {
    Available,
    Lost
}
