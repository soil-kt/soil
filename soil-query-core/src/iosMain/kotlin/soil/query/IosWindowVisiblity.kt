// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.UIKit.UIApplicationDidBecomeActiveNotification
import platform.UIKit.UIApplicationWillResignActiveNotification
import platform.darwin.NSObject
import soil.query.core.Notifier
import soil.query.core.WindowVisibility
import soil.query.core.WindowVisibilityEvent
import soil.query.core.WindowVisibilityProvider

/**
 * Implementation of [WindowVisibility] for iOS.
 *
 * In the iOS system, [UIApplicationDidBecomeActiveNotification] and [UIApplicationWillResignActiveNotification]
 * are used to monitor window visibility states.
 * It notifies the window visibility state based on the notification received.
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
class IosWindowVisibility(
    private val notificationCenter: NSNotificationCenter = NSNotificationCenter.defaultCenter
) : WindowVisibilityProvider() {

    override fun createReceiver(): Receiver = Monitor(
        notificationCenter = notificationCenter,
        notifier = this
    )

    private class Monitor(
        private val notificationCenter: NSNotificationCenter,
        notifier: Notifier<WindowVisibilityEvent>
    ) : Receiver {

        private val notification = Notification(notifier)

        override fun start() {
            notificationCenter.addObserver(
                observer = notification,
                selector = NSSelectorFromString(Notification::appDidBecomeActive.name + ":"),
                name = UIApplicationDidBecomeActiveNotification,
                `object` = null
            )
            notificationCenter.addObserver(
                observer = notification,
                selector = NSSelectorFromString(Notification::appWillResignActive.name + ":"),
                name = UIApplicationWillResignActiveNotification,
                `object` = null
            )
        }

        override fun stop() {
            notificationCenter.removeObserver(
                observer = notification,
                name = UIApplicationDidBecomeActiveNotification,
                `object` = null
            )
            notificationCenter.removeObserver(
                observer = notification,
                name = UIApplicationWillResignActiveNotification,
                `object` = null
            )
        }
    }

    // NOTE: The Monitor class cannot directly inherit from NSObject because it already
    // implements the Receiver interface. The Kotlin compiler error states:
    // "Mixing Kotlin and Objective-C supertypes is not supported"
    // This is why the notification handling is separated into this dedicated class.
    private class Notification(
        private val notifier: Notifier<WindowVisibilityEvent>
    ) : NSObject() {
        @Suppress("unused", "UNUSED_PARAMETER")
        @ObjCAction
        fun appDidBecomeActive(arg: NSNotification) {
            notifier.notify(WindowVisibilityEvent.Foreground)
        }

        @Suppress("unused", "UNUSED_PARAMETER")
        @ObjCAction
        fun appWillResignActive(arg: NSNotification) {
            notifier.notify(WindowVisibilityEvent.Background)
        }
    }
}
