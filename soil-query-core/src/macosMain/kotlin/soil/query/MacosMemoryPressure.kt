// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.AppKit.NSApplicationDidHideNotification
import platform.darwin.NSObject
import soil.query.core.MemoryPressure
import soil.query.core.MemoryPressureLevel

/**
 * Implementation of [MemoryPressure] for macOS.
 *
 * In the macOS system, [NSNotificationCenter] is used to monitor memory pressure states.
 * Note: macOS has different memory management compared to iOS, so we use application state
 * changes as a proxy for memory pressure.
 *
 *    | macOS Notification                      | MemoryPressureLevel   |
 *    |:----------------------------------------|:----------------------|
 *    | NSApplicationDidHide                    | Low                   |
 *
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
class MacosMemoryPressure : MemoryPressure {

    private var obw: ObserverWrapper? = null

    override fun addObserver(observer: MemoryPressure.Observer) {
        val nativeObserver = ObserverWrapper(observer).also { obw = it }
        NSNotificationCenter.defaultCenter.addObserver(
            observer = nativeObserver,
            selector = NSSelectorFromString(ObserverWrapper::appDidHide.name + ":"),
            name = NSApplicationDidHideNotification,
            `object` = null
        )
    }

    override fun removeObserver(observer: MemoryPressure.Observer) {
        val nativeObserver = obw ?: return
        NSNotificationCenter.defaultCenter.removeObserver(
            observer = nativeObserver,
            name = NSApplicationDidHideNotification,
            `object` = null
        )
        obw = null
    }

    class ObserverWrapper(
        private val observer: MemoryPressure.Observer
    ) : NSObject() {

        @Suppress("unused", "UNUSED_PARAMETER")
        @ObjCAction
        fun appDidHide(arg: NSNotification) {
            observer.onReceive(MemoryPressureLevel.Low)
        }
    }
}
