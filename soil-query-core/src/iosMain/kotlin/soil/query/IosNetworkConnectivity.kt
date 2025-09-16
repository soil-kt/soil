// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import platform.Network.nw_path_get_status
import platform.Network.nw_path_monitor_cancel
import platform.Network.nw_path_monitor_create
import platform.Network.nw_path_monitor_set_queue
import platform.Network.nw_path_monitor_set_update_handler
import platform.Network.nw_path_monitor_start
import platform.Network.nw_path_monitor_t
import platform.Network.nw_path_status_invalid
import platform.Network.nw_path_status_satisfiable
import platform.Network.nw_path_status_satisfied
import platform.Network.nw_path_status_unsatisfied
import platform.darwin.dispatch_get_main_queue
import soil.query.core.AbstractNotifier
import soil.query.core.Listener
import soil.query.core.NetworkConnectivity
import soil.query.core.NetworkConnectivityEvent
import soil.query.core.Notifier

/**
 * Implementation of [NetworkConnectivity] for iOS using NWPathMonitor.
 */
class IosNetworkConnectivity : AbstractNotifier<NetworkConnectivityEvent>(), NetworkConnectivity {

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
        private val notifier: Notifier<NetworkConnectivityEvent>
    ) {
        private var nwPathMonitor: nw_path_monitor_t? = null

        private var isOnline: Boolean? = null

        private val handleOnline: () -> Unit = {
            if (isOnline != true) {
                isOnline = true
                notifyListeners()
            }
        }

        private val handleOffline: () -> Unit = {
            if (isOnline != false) {
                isOnline = false
                notifyListeners()
            }
        }

        fun start() {
            val monitor = nw_path_monitor_create().also { nwPathMonitor = it }
            nw_path_monitor_set_queue(monitor, dispatch_get_main_queue())
            nw_path_monitor_set_update_handler(monitor) { path ->
                if (path != null) {
                    when (nw_path_get_status(path)) {
                        nw_path_status_satisfied -> handleOnline()
                        nw_path_status_unsatisfied -> handleOffline()
                        nw_path_status_satisfiable,
                        nw_path_status_invalid -> Unit
                    }
                }
            }
            nw_path_monitor_start(nwPathMonitor)
        }

        fun stop() {
            val monitor = nwPathMonitor
            if (monitor != null) {
                nw_path_monitor_cancel(monitor)
            }
            nwPathMonitor = null
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
            fun create(
                notifier: Notifier<NetworkConnectivityEvent>
            ) = Monitor(
                notifier = notifier
            )
        }
    }
}
