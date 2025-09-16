// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import soil.query.core.MemoryPressure
import soil.query.core.MemoryPressureLevel
import soil.query.core.MemoryPressureProvider
import soil.query.core.Notifier

/**
 * Implementation of [MemoryPressure] for Android.
 *
 * In the Android system, [ComponentCallbacks2] is used to monitor memory pressure states.
 * It notifies the memory pressure state based on the level parameter of [ComponentCallbacks2.onTrimMemory].
 *
 *     | Android Trim Level             | MemoryPressureLevel   |
 *     |:-------------------------------|:----------------------|
 *     | TRIM_MEMORY_UI_HIDDEN          | Low                   |
 *     | TRIM_MEMORY_BACKGROUND         | Low                   |
 *     | TRIM_MEMORY_MODERATE           | Low                   |
 *     | TRIM_MEMORY_RUNNING_MODERATE   | Low                   |
 *     | TRIM_MEMORY_RUNNING_LOW        | Low                   |
 *     | TRIM_MEMORY_COMPLETE           | Critical              |
 *     | TRIM_MEMORY_RUNNING_CRITICAL   | Critical              |
 *
 */
class AndroidMemoryPressure(
    private val context: Context
) : MemoryPressureProvider() {

    override fun createReceiver(): Receiver = Monitor(
        register = context::registerComponentCallbacks,
        unregister = context::unregisterComponentCallbacks,
        notifier = this
    )

    private class Monitor(
        val register: (ComponentCallbacks2) -> Unit,
        val unregister: (ComponentCallbacks2) -> Unit,
        val notifier: Notifier<MemoryPressureLevel>
    ) : Receiver, ComponentCallbacks2 {

        override fun onConfigurationChanged(newConfig: Configuration) = Unit

        @Deprecated("Deprecated in Java")
        override fun onLowMemory() {
            notifier.notify(MemoryPressureLevel.High)
        }

        override fun onTrimMemory(level: Int) {
            when (level) {
                ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN,
                ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
                ComponentCallbacks2.TRIM_MEMORY_MODERATE,
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                    notifier.notify(MemoryPressureLevel.Low)
                }

                ComponentCallbacks2.TRIM_MEMORY_COMPLETE,
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                    notifier.notify(MemoryPressureLevel.High)
                }
            }
        }

        override fun start() = register(this)

        override fun stop() = unregister(this)
    }
}
