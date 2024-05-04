// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import soil.query.internal.NetworkConnectivity
import soil.query.internal.NetworkConnectivityEvent

/**
 * Implementation of [NetworkConnectivity] for Android.
 *
 * In the Android system, [ConnectivityManager] is used to monitor network connectivity states.
 *
 * **Note**: This implementation requires the `ACCESS_NETWORK_STATE` permission.
 *
 * ```
 * <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
 * ```
 */
class AndroidNetworkConnectivity(
    private val context: Context
) : NetworkConnectivity {

    private val request = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .build()

    private val manager: ConnectivityManager
        get() = context.getSystemService(ConnectivityManager::class.java)

    private var obw: ObserverWrapper? = null

    @RequiresPermission(android.Manifest.permission.ACCESS_NETWORK_STATE)
    override fun addObserver(observer: NetworkConnectivity.Observer) {
        manager.registerNetworkCallback(request, ObserverWrapper(observer).also { obw = it })
    }

    override fun removeObserver(observer: NetworkConnectivity.Observer) {
        obw?.let { manager.unregisterNetworkCallback(it) }
        obw = null
    }

    /**
     * Implementation of [ConnectivityManager.NetworkCallback] for observing network connectivity.
     */
    class ObserverWrapper(
        private val observer: NetworkConnectivity.Observer
    ) : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            observer.onReceive(NetworkConnectivityEvent.Available)
        }

        override fun onLost(network: Network) {
            observer.onReceive(NetworkConnectivityEvent.Lost)
        }
    }
}
