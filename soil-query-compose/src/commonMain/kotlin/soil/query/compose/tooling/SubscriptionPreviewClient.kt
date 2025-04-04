// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.tooling

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import soil.query.SubscriptionClient
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionReceiver
import soil.query.SubscriptionRef
import soil.query.SubscriptionState
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.core.Marker
import soil.query.core.UniqueId

/**
 * Usage:
 * ```kotlin
 * val subscriptionClient = SubscriptionPreviewClient {
 *     on(MySubscriptionId1) { SubscriptionState.success("data") }
 *     on(MySubscriptionId2) { .. }
 * }
 * ```
 */
@Stable
class SubscriptionPreviewClient(
    private val previewData: Map<UniqueId, SubscriptionState<*>>
) : SubscriptionClient {

    override val subscriptionReceiver = SubscriptionReceiver

    @ExperimentalSoilQueryApi
    @Suppress("UNCHECKED_CAST")
    override fun <T> getSubscription(
        key: SubscriptionKey<T>,
        marker: Marker
    ): SubscriptionRef<T> {
        val state = previewData[key.id] as? SubscriptionState<T> ?: SubscriptionState.initial()
        return SnapshotSubscription(key.id, MutableStateFlow(state))
    }

    private class SnapshotSubscription<T>(
        override val id: SubscriptionId<T>,
        override val state: StateFlow<SubscriptionState<T>>
    ) : SubscriptionRef<T> {
        override fun close() = Unit
        override suspend fun reset() = Unit
        override suspend fun resume() = Unit
        override suspend fun join() = Unit
    }

    /**
     * Builder for [SubscriptionPreviewClient].
     */
    class Builder {
        private val previewData = mutableMapOf<UniqueId, SubscriptionState<*>>()

        fun <T> on(id: SubscriptionId<T>, snapshot: () -> SubscriptionState<T>) {
            previewData[id] = snapshot()
        }

        fun build() = SubscriptionPreviewClient(previewData)
    }
}

/**
 * Create a [SubscriptionPreviewClient] instance with the provided [initializer].
 */
fun SubscriptionPreviewClient(initializer: SubscriptionPreviewClient.Builder.() -> Unit): SubscriptionPreviewClient {
    return SubscriptionPreviewClient.Builder().apply(initializer).build()
}
