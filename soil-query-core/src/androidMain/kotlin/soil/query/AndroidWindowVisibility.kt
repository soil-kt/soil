// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import soil.query.core.WindowVisibility
import soil.query.core.WindowVisibilityEvent

/**
 * Implementation of [WindowVisibility] for Android.
 *
 * In the Android system, [ProcessLifecycleOwner] is used to monitor window visibility states.
 * It notifies the window visibility state based on the lifecycle state of [ProcessLifecycleOwner].
 */
class AndroidWindowVisibility(
    private val lifecycleOwner: LifecycleOwner = ProcessLifecycleOwner.get()
) : WindowVisibility {

    // NOTE: ProcessLifecycleOwner only supports calls from the main thread.
    private val mainHandler = Handler(Looper.getMainLooper())
    private var cbw: CallbackWrapper? = null

    override fun addObserver(observer: WindowVisibility.Observer) {
        if (mainHandler.looper.isCurrentThread) {
            lifecycleOwner.lifecycle.addObserver(CallbackWrapper(observer).also { cbw = it })
        } else {
            mainHandler.post { addObserver(observer) }
        }
    }

    override fun removeObserver(observer: WindowVisibility.Observer) {
        if (mainHandler.looper.isCurrentThread) {
            cbw?.let { lifecycleOwner.lifecycle.removeObserver(it) }
            cbw = null
        } else {
            mainHandler.post { removeObserver(observer) }
        }
    }

    /**
     * Implementation of [DefaultLifecycleObserver] for observing window visibility.
     */
    class CallbackWrapper(
        private val callback: WindowVisibility.Observer
    ) : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            callback.onReceive(WindowVisibilityEvent.Foreground)
        }

        override fun onStop(owner: LifecycleOwner) {
            callback.onReceive(WindowVisibilityEvent.Background)
        }
    }
}
