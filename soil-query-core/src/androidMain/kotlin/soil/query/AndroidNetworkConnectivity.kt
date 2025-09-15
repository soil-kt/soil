// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.annotation.RequiresPermission
import soil.query.core.AbstractNotifier
import soil.query.core.Listener
import soil.query.core.NetworkConnectivity
import soil.query.core.NetworkConnectivityEvent
import soil.query.core.Notifier

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
) : AbstractNotifier<NetworkConnectivityEvent>(), NetworkConnectivity {

    private var monitor: Monitor? = null

    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    override fun addListener(listener: Listener<NetworkConnectivityEvent>) {
        super.addListener(listener)
        if (monitor == null) {
            monitor = Monitor.create(context = context, notifier = this)
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
        private val connectivityManager: ConnectivityManager,
        private val notifier: Notifier<NetworkConnectivityEvent>
    ) : ConnectivityManager.NetworkCallback() {
        private var isOnline: Boolean? = null

        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        override fun onLost(network: Network) {
            if (hasValidatedNetwork()) return
            if (isOnline == false) return
            isOnline = false
            notifyListeners()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            if (!networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) return
            if (isOnline == true) return
            isOnline = true
            notifyListeners()
        }

        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        fun start() {
            if (isOnline == null) {
                isOnline = hasValidatedNetwork()
                notifyListeners()
            }
            connectivityManager.registerNetworkCallback(
                NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build(),
                this
            )
        }

        fun stop() {
            connectivityManager.unregisterNetworkCallback(this)
            isOnline = null
        }

        private fun notifyListeners() {
            when (isOnline) {
                true -> notifier.notify(NetworkConnectivityEvent.Available)
                false -> notifier.notify(NetworkConnectivityEvent.Lost)
                null -> Unit
            }
        }

        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        private fun hasValidatedNetwork(): Boolean {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        }

        companion object {
            fun create(
                context: Context,
                notifier: Notifier<NetworkConnectivityEvent>
            ) = Monitor(
                connectivityManager = context.getSystemService(ConnectivityManager::class.java),
                notifier = notifier
            )
        }
    }
}
