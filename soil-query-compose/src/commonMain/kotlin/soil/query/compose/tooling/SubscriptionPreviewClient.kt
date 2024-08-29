// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.tooling

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import soil.query.SubscriptionClient
import soil.query.SubscriptionCommand
import soil.query.SubscriptionId
import soil.query.SubscriptionKey
import soil.query.SubscriptionOptions
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
    private val previewData: Map<UniqueId, SubscriptionState<*>>,
    override val defaultSubscriptionOptions: SubscriptionOptions = SubscriptionOptions()
) : SubscriptionClient {

    @ExperimentalSoilQueryApi
    @Suppress("UNCHECKED_CAST")
    override fun <T> getSubscription(
        key: SubscriptionKey<T>,
        marker: Marker
    ): SubscriptionRef<T> {
        val state = previewData[key.id] as? SubscriptionState<T> ?: SubscriptionState.initial()
        val options = key.onConfigureOptions()?.invoke(defaultSubscriptionOptions) ?: defaultSubscriptionOptions
        return SnapshotSubscription(key, options, marker, MutableStateFlow(state))
    }

    private class SnapshotSubscription<T>(
        override val key: SubscriptionKey<T>,
        override val options: SubscriptionOptions,
        override val marker: Marker,
        override val state: StateFlow<SubscriptionState<T>>
    ) : SubscriptionRef<T> {
        override fun launchIn(scope: CoroutineScope): Job = Job()
        override suspend fun send(command: SubscriptionCommand<T>) = Unit
        override suspend fun resume() = Unit
        override fun cancel() = Unit
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

