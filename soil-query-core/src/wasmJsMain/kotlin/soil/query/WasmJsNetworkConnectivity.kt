// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.browser.window
import org.w3c.dom.Window
import org.w3c.dom.events.Event
import soil.query.core.AbstractNotifier
import soil.query.core.Listener
import soil.query.core.NetworkConnectivity
import soil.query.core.NetworkConnectivityEvent
import soil.query.core.Notifier

/**
 * Implementation of [NetworkConnectivity] for WasmJs.
 */
class WasmJsNetworkConnectivity : AbstractNotifier<NetworkConnectivityEvent>(), NetworkConnectivity {

    private var monitor: Monitor? = null

    override fun addListener(listener: Listener<NetworkConnectivityEvent>) {
        super.addListener(listener)
        if (monitor == null) {
            monitor = Monitor.create(notifier = this)
            monitor?.start()
        }
    }

    override fun removeListener(listener: Listener<NetworkConnectivityEvent>) {
        super.removeListener(listener)
        if (!hasListeners()) {
            monitor?.stop()
            monitor = null
        }
    }

    internal class Monitor(
        private val window: Window,
        private val notifier: Notifier<NetworkConnectivityEvent>
    ) {
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

        fun start() {
            if (isOnline == null) {
                isOnline = window.navigator.onLine
                notifyListeners()
            }
            window.addEventListener(TYPE_ONLINE, handleOnline)
            window.addEventListener(TYPE_OFFLINE, handleOffline)
        }

        fun stop() {
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

        companion object {
            // https://developer.mozilla.org/en-US/docs/Web/API/Window/online_event
            private const val TYPE_ONLINE = "online"

            // https://developer.mozilla.org/en-US/docs/Web/API/Window/offline_event
            private const val TYPE_OFFLINE = "offline"

            fun create(
                notifier: Notifier<NetworkConnectivityEvent>
            ) = Monitor(
                window = window,
                notifier = notifier
            )
        }
    }
}
