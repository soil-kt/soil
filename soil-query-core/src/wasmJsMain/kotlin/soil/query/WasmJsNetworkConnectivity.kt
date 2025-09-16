// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.browser.window
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import soil.query.core.NetworkConnectivity
import soil.query.core.NetworkConnectivityEvent
import soil.query.core.NetworkConnectivityProvider
import soil.query.core.Notifier

/**
 * Implementation of [NetworkConnectivity] for WasmJs.
 */
class WasmJsNetworkConnectivity : NetworkConnectivityProvider() {

    override fun createReceiver(): Receiver = Monitor(
        window = window,
        notifier = this
    )

    private class Monitor(
        private val window: Window,
        private val notifier: Notifier<NetworkConnectivityEvent>
    ) : Receiver {

        private var isOnline: Boolean? = null

        private val handleOnline: (Event) -> Unit = {
            if (isOnline != true) {
                isOnline = true
                notifyListeners()
            }
        }

        private val handleOffline: (Event) -> Unit = {
            if (isOnline != false) {
                isOnline = false
                notifyListeners()
            }
        }

        override fun start() {
            if (isOnline == null) {
                isOnline = window.navigator.onLine
                notifyListeners()
            }
            window.addEventListener(TYPE_ONLINE, handleOnline)
            window.addEventListener(TYPE_OFFLINE, handleOffline)
        }

        override fun stop() {
            window.removeEventListener(TYPE_ONLINE, handleOnline)
            window.removeEventListener(TYPE_OFFLINE, handleOffline)
            isOnline = null
        }

        private fun notifyListeners() {
            when (isOnline) {
                true -> notifier.notify(NetworkConnectivityEvent.Available)
                false -> notifier.notify(NetworkConnectivityEvent.Lost)
                null -> Unit
            }
        }
    }
}

// https://developer.mozilla.org/en-US/docs/Web/API/Window/online_event
private const val TYPE_ONLINE = "online"

// https://developer.mozilla.org/en-US/docs/Web/API/Window/offline_event
private const val TYPE_OFFLINE = "offline"
