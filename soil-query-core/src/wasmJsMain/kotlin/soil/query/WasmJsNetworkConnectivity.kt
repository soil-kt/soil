// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.browser.window
import org.w3c.dom.events.Event
import soil.query.internal.NetworkConnectivity
import soil.query.internal.NetworkConnectivityEvent

/**
 * Implementation of [NetworkConnectivity] for WasmJs.
 */
class WasmJsNetworkConnectivity : NetworkConnectivity {

    private var onlineListener: ((Event) -> Unit)? = null
    private var offlineListener: ((Event) -> Unit)? = null

    override fun addObserver(observer: NetworkConnectivity.Observer) {
        onlineListener = { observer.onReceive(NetworkConnectivityEvent.Available) }
        offlineListener = { observer.onReceive(NetworkConnectivityEvent.Lost) }
        window.addEventListener(TYPE_ONLINE, onlineListener)
        window.addEventListener(TYPE_OFFLINE, offlineListener)
    }

    override fun removeObserver(observer: NetworkConnectivity.Observer) {
        onlineListener?.let { window.removeEventListener(TYPE_ONLINE, it) }
        offlineListener?.let { window.removeEventListener(TYPE_OFFLINE, it) }
        onlineListener = null
        offlineListener = null
    }

    companion object {
        // https://developer.mozilla.org/en-US/docs/Web/API/Window/online_event
        private const val TYPE_ONLINE = "online"
        // https://developer.mozilla.org/en-US/docs/Web/API/Window/offline_event
        private const val TYPE_OFFLINE = "offline"
    }
}
