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
import soil.query.core.MemoryPressure
import soil.query.core.MemoryPressureLevel
import soil.query.core.MemoryPressureProvider
import soil.query.core.Notifier

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
class IosMemoryPressure(
    private val notificationCenter: NSNotificationCenter = NSNotificationCenter.defaultCenter
) : MemoryPressureProvider() {

    override fun createReceiver(): Receiver = Monitor(
        notificationCenter = notificationCenter,
        notifier = this
    )

    private class Monitor(
        private val notificationCenter: NSNotificationCenter,
        notifier: Notifier<MemoryPressureLevel>
    ) : Receiver {

        private val notification = Notification(notifier)

        override fun start() {
            notificationCenter.addObserver(
                observer = notification,
                selector = NSSelectorFromString(Notification::appDidEnterBackground.name + ":"),
                name = UIApplicationDidEnterBackgroundNotification,
                `object` = null
            )
            notificationCenter.addObserver(
                observer = notification,
                selector = NSSelectorFromString(Notification::appDidReceiveMemoryWarning.name + ":"),
                name = UIApplicationDidReceiveMemoryWarningNotification,
                `object` = null
            )
        }

        override fun stop() {
            notificationCenter.removeObserver(
                observer = notification,
                name = UIApplicationDidEnterBackgroundNotification,
                `object` = null
            )
            notificationCenter.removeObserver(
                observer = notification,
                name = UIApplicationDidReceiveMemoryWarningNotification,
                `object` = null
            )
        }
    }

    // NOTE: The Monitor class cannot directly inherit from NSObject because it already
    // implements the Receiver interface. The Kotlin compiler error states:
    // "Mixing Kotlin and Objective-C supertypes is not supported"
    // This is why the notification handling is separated into this dedicated class.
    private class Notification(
        private val notifier: Notifier<MemoryPressureLevel>
    ) : NSObject() {
        @Suppress("unused", "UNUSED_PARAMETER")
        @ObjCAction
        fun appDidEnterBackground(arg: NSNotification) {
            notifier.notify(MemoryPressureLevel.Low)
        }

        @Suppress("unused", "UNUSED_PARAMETER")
        @ObjCAction
        fun appDidReceiveMemoryWarning(arg: NSNotification) {
            notifier.notify(MemoryPressureLevel.High)
        }
    }
}
