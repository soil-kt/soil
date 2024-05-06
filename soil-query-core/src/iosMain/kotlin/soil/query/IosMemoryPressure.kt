// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplicationDidEnterBackgroundNotification
import platform.UIKit.UIApplicationDidReceiveMemoryWarningNotification
import platform.darwin.NSObject
import soil.query.internal.MemoryPressure
import soil.query.internal.MemoryPressureLevel

/**
 * Implementation of [MemoryPressure] for iOS.
 *
 * In the iOS system, [NSNotificationCenter] is used to monitor memory pressure states.
 *
 *    | iOS Notification                        | MemoryPressureLevel   |
 *    |:----------------------------------------|:----------------------|
 *    | UIApplicationDidEnterBackground         | Low                   |
 *    | UIApplicationDidReceiveMemoryWarning    | Critical              |
 *
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
class IosMemoryPressure : MemoryPressure {

    private var obw: ObserverWrapper? = null

    override fun addObserver(observer: MemoryPressure.Observer) {
        val nativeObserver = ObserverWrapper(observer).also { obw = it }
        NSNotificationCenter.defaultCenter.addObserver(
            observer = nativeObserver,
            selector = NSSelectorFromString(ObserverWrapper::appDidEnterBackground.name + ":"),
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null
        )
        NSNotificationCenter.defaultCenter.addObserver(
            observer = nativeObserver,
            selector = NSSelectorFromString(ObserverWrapper::appDidReceiveMemoryWarning.name + ":"),
            name = UIApplicationDidReceiveMemoryWarningNotification,
            `object` = null
        )
    }

    override fun removeObserver(observer: MemoryPressure.Observer) {
        val nativeObserver = obw ?: return
        NSNotificationCenter.defaultCenter.removeObserver(
            observer = nativeObserver,
            name = UIApplicationDidEnterBackgroundNotification,
            `object` = null
        )
        NSNotificationCenter.defaultCenter.removeObserver(
            observer = nativeObserver,
            name = UIApplicationDidReceiveMemoryWarningNotification,
            `object` = null
        )
        obw = null
    }

    class ObserverWrapper(
        private val observer: MemoryPressure.Observer
    ) : NSObject() {

        @Suppress("unused", "UNUSED_PARAMETER")
        @ObjCAction
        fun appDidEnterBackground(arg: NSNotification) {
            observer.onReceive(MemoryPressureLevel.Low)
        }

        @Suppress("unused", "UNUSED_PARAMETER")
        @ObjCAction
        fun appDidReceiveMemoryWarning(arg: NSNotification) {
            observer.onReceive(MemoryPressureLevel.Critical)
        }
    }
}
