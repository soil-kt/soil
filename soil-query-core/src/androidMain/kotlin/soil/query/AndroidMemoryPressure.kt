// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import soil.query.internal.MemoryPressure
import soil.query.internal.MemoryPressureLevel

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
) : MemoryPressure {

    private var obw: ObserverWrapper? = null
    override fun addObserver(observer: MemoryPressure.Observer) {
        context.registerComponentCallbacks(ObserverWrapper(observer).also { obw = it })
    }

    override fun removeObserver(observer: MemoryPressure.Observer) {
        obw?.let { context.unregisterComponentCallbacks(it) }
        obw = null
    }

    /**
     * Implementation of [ComponentCallbacks2] for observing memory pressure.
     */
    class ObserverWrapper(
        private val observer: MemoryPressure.Observer
    ) : ComponentCallbacks2 {

        override fun onConfigurationChanged(newConfig: Configuration) = Unit

        override fun onLowMemory() {
            observer.onReceive(MemoryPressureLevel.High)
        }

        override fun onTrimMemory(level: Int) {
            when (level) {
                ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN,
                ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
                ComponentCallbacks2.TRIM_MEMORY_MODERATE,
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE,
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> {
                    observer.onReceive(MemoryPressureLevel.Low)
                }

                ComponentCallbacks2.TRIM_MEMORY_COMPLETE,
                ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> {
                    observer.onReceive(MemoryPressureLevel.High)
                }
            }
        }
    }
}
