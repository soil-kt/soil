// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import platform.Foundation.NSNotification
import platform.Foundation.NSNotificationCenter
import platform.Foundation.NSSelectorFromString
import platform.AppKit.NSApplicationDidBecomeActiveNotification
import platform.AppKit.NSApplicationDidResignActiveNotification
import platform.darwin.NSObject
import soil.query.core.WindowVisibility
import soil.query.core.WindowVisibilityEvent

/**
 * Implementation of [WindowVisibility] for macOS.
 *
 * In the macOS system, [NSApplicationDidBecomeActiveNotification] and [NSApplicationDidResignActiveNotification]
 * are used to monitor window visibility states.
 * It notifies the window visibility state based on the notification received.
 */
@OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
class MacosWindowVisibility : WindowVisibility {

    private var obw: ObserverWrapper? = null

    override fun addObserver(observer: WindowVisibility.Observer) {
        val nativeObserver = ObserverWrapper(observer).also { obw = it }
        NSNotificationCenter.defaultCenter.addObserver(
            observer = nativeObserver,
            selector = NSSelectorFromString(ObserverWrapper::appDidBecomeActive.name + ":"),
            name = NSApplicationDidBecomeActiveNotification,
            `object` = null
        )
        NSNotificationCenter.defaultCenter.addObserver(
            observer = nativeObserver,
            selector = NSSelectorFromString(ObserverWrapper::appDidResignActive.name + ":"),
            name = NSApplicationDidResignActiveNotification,
            `object` = null
        )
    }

    override fun removeObserver(observer: WindowVisibility.Observer) {
        val nativeObserver = obw ?: return
        NSNotificationCenter.defaultCenter.removeObserver(
            observer = nativeObserver,
            name = NSApplicationDidBecomeActiveNotification,
            `object` = null
        )
        NSNotificationCenter.defaultCenter.removeObserver(
            observer = nativeObserver,
            name = NSApplicationDidResignActiveNotification,
            `object` = null
        )
        obw = null
    }

    class ObserverWrapper(
        private val observer: WindowVisibility.Observer
    ) : NSObject() {

        @Suppress("unused", "UNUSED_PARAMETER")
        @ObjCAction
        fun appDidBecomeActive(arg: NSNotification) {
            observer.onReceive(WindowVisibilityEvent.Foreground)
        }

        @Suppress("unused", "UNUSED_PARAMETER")
        @ObjCAction
        fun appDidResignActive(arg: NSNotification) {
            observer.onReceive(WindowVisibilityEvent.Background)
        }
    }
}
