// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import soil.query.core.Notifier
import soil.query.core.WindowVisibility
import soil.query.core.WindowVisibilityEvent
import soil.query.core.WindowVisibilityProvider

/**
 * Implementation of [WindowVisibility] for Android.
 *
 * In the Android system, [ProcessLifecycleOwner] is used to monitor window visibility states.
 * It notifies the window visibility state based on the lifecycle state of [ProcessLifecycleOwner].
 */
class AndroidWindowVisibility(
    private val lifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get()
) : WindowVisibilityProvider() {

    override fun createReceiver(): Receiver = Monitor(
        lifecycleOwner = lifecycleOwner,
        notifier = this
    )

    /**
     * Implementation of [DefaultLifecycleObserver] for observing window visibility.
     */
    private class Monitor(
        private val lifecycleOwner: LifecycleOwner,
        private val notifier: Notifier<WindowVisibilityEvent>
    ) : DefaultLifecycleObserver, Receiver {

        // NOTE: ProcessLifecycleOwner only supports calls from the main thread.
        private val mainHandler = Handler(Looper.getMainLooper())
        private var runnable: Runnable? = null

        override fun onStart(owner: LifecycleOwner) {
            notifier.notify(WindowVisibilityEvent.Foreground)
        }

        override fun onStop(owner: LifecycleOwner) {
            notifier.notify(WindowVisibilityEvent.Background)
        }

        override fun start() {
            ensureCancelRunnable()
            if (mainHandler.looper.isCurrentThread) {
                lifecycleOwner.lifecycle.addObserver(this)
            } else {
                val r = Runnable { start() }.also { runnable = it }
                mainHandler.post(r)
            }
        }

        override fun stop() {
            ensureCancelRunnable()
            if (mainHandler.looper.isCurrentThread) {
                lifecycleOwner.lifecycle.removeObserver(this)
            } else {
                val r = Runnable { stop() }.also { runnable = it }
                mainHandler.post(r)
            }
        }

        private fun ensureCancelRunnable() {
            runnable?.let(mainHandler::removeCallbacks)
            runnable = null
        }
    }
}
