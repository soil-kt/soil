// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import platform.Network.*
import platform.darwin.dispatch_get_main_queue
import soil.query.core.NetworkConnectivity
import soil.query.core.NetworkConnectivityEvent
import kotlinx.cinterop.*

/**
 * Implementation of [NetworkConnectivity] for iOS using NWPathMonitor.
 */
class IosNetworkConnectivity : NetworkConnectivity {

    private var monitor: nw_path_monitor_t? = null
    private var currentObserver: NetworkConnectivity.Observer? = null

    override fun addObserver(observer: NetworkConnectivity.Observer) {
        removeObserver(observer)
        currentObserver = observer
        monitor = nw_path_monitor_create()

        monitor?.let { pathMonitor ->
            nw_path_monitor_set_queue(pathMonitor, dispatch_get_main_queue())
            nw_path_monitor_set_update_handler(pathMonitor) { path ->
                if (path != null) {
                    when (nw_path_get_status(path)) {
                        nw_path_status_satisfied -> {
                            observer.onReceive(NetworkConnectivityEvent.Available)
                        }
                        nw_path_status_unsatisfied -> {
                            observer.onReceive(NetworkConnectivityEvent.Lost)
                        }
                        nw_path_status_satisfiable,
                        nw_path_status_invalid -> Unit
                    }
                }
            }
            nw_path_monitor_start(pathMonitor)
        }
    }

    override fun removeObserver(observer: NetworkConnectivity.Observer) {
        monitor?.let { pathMonitor ->
            nw_path_monitor_cancel(pathMonitor)
        }
        monitor = null
        currentObserver = null
    }
}
