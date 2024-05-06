// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.browser.document
import org.w3c.dom.events.Event
import soil.query.internal.WindowVisibility
import soil.query.internal.WindowVisibilityEvent
import soil.query.internal.document as documentAlt

/**
 * Implementation of [WindowVisibility] for WasmJs.
 */
class WasmJsWindowVisibility : WindowVisibility {

    private var visibilityListener: ((Event) -> Unit)? = null

    override fun addObserver(observer: WindowVisibility.Observer) {
        visibilityListener = {
            if (documentAlt.visibilityState == VISIBILITY_STATE_VISIBLE) {
                observer.onReceive(WindowVisibilityEvent.Foreground)
            } else {
                observer.onReceive(WindowVisibilityEvent.Background)
            }
        }
        document.addEventListener(TYPE_VISIBILITY_CHANGE, visibilityListener)
    }

    override fun removeObserver(observer: WindowVisibility.Observer) {
        visibilityListener?.let { document.removeEventListener(TYPE_VISIBILITY_CHANGE, it) }
        visibilityListener = null
    }

    companion object {
        // https://developer.mozilla.org/en-US/docs/Web/API/Document/visibilitychange_event
        @Suppress("SpellCheckingInspection")
        private const val TYPE_VISIBILITY_CHANGE = "visibilitychange"
        private const val VISIBILITY_STATE_VISIBLE = "visible"
    }
}
