// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.browser.document
import org.w3c.dom.Document
import org.w3c.dom.events.Event
import soil.query.core.Notifier
import soil.query.core.WindowVisibility
import soil.query.core.WindowVisibilityEvent
import soil.query.core.WindowVisibilityProvider
import soil.query.core.document as documentAlt

/**
 * Implementation of [WindowVisibility] for WasmJs.
 */
class WasmJsWindowVisibility : WindowVisibilityProvider() {

    override fun createReceiver(): Receiver = Monitor(
        document = document,
        visibilityState = { documentAlt.visibilityState },
        notifier = this
    )

    private class Monitor(
        private val document: Document,
        visibilityState: () -> String,
        notifier: Notifier<WindowVisibilityEvent>
    ) : Receiver {

        private var visibilityListener: (Event) -> Unit = {
            if (visibilityState() == VISIBILITY_STATE_VISIBLE) {
                notifier.notify(WindowVisibilityEvent.Foreground)
            } else {
                notifier.notify(WindowVisibilityEvent.Background)
            }
        }

        override fun start() {
            document.addEventListener(TYPE_VISIBILITY_CHANGE, visibilityListener)
        }

        override fun stop() {
            document.removeEventListener(TYPE_VISIBILITY_CHANGE, visibilityListener)
        }
    }
}

// https://developer.mozilla.org/en-US/docs/Web/API/Document/visibilitychange_event
@Suppress("SpellCheckingInspection")
private const val TYPE_VISIBILITY_CHANGE = "visibilitychange"
private const val VISIBILITY_STATE_VISIBLE = "visible"
