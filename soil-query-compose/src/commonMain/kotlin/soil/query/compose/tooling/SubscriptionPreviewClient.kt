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
import soil.query.SubscriptionTestTag
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.core.Marker
import soil.query.core.TestTag
import soil.query.core.UniqueId
import soil.query.marker.TestTagMarker

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
    private val previewDataByTag: Map<TestTag, SubscriptionState<*>>
) : SubscriptionClient {

    override val subscriptionReceiver = SubscriptionReceiver

    @ExperimentalSoilQueryApi
    @Suppress("UNCHECKED_CAST")
    override fun <T> getSubscription(
        key: SubscriptionKey<T>,
        marker: Marker
    ): SubscriptionRef<T> {
        val state = previewData[key.id] as? SubscriptionState<T>
            ?: marker[TestTagMarker.Key]?.value?.let { previewDataByTag[it] as? SubscriptionState<T> }
            ?: SubscriptionState.initial()
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
        private val previewDataByTag = mutableMapOf<TestTag, SubscriptionState<*>>()

        /**
         * Registers a preview state for the subscription with the specified ID.
         *
         * @param id The subscription ID that identifies this subscription
         * @param snapshot A function that provides the subscription state to be returned for this ID
         */
        fun <T> on(id: SubscriptionId<T>, snapshot: () -> SubscriptionState<T>) {
            previewData[id] = snapshot()
        }

        /**
         * Registers a preview state for the subscription with the specified test tag.
         *
         * @param testTag The test tag that identifies this subscription
         * @param snapshot A function that provides the subscription state to be returned for this tag
         */
        fun <T> on(testTag: SubscriptionTestTag<T>, snapshot: () -> SubscriptionState<T>) {
            previewDataByTag[testTag] = snapshot()
        }

        /**
         * Builds a new instance of [SubscriptionPreviewClient] with the registered preview states.
         *
         * @return A new [SubscriptionPreviewClient] instance
         */
        fun build() = SubscriptionPreviewClient(previewData, previewDataByTag)
    }
}

/**
 * Create a [SubscriptionPreviewClient] instance with the provided [initializer].
 *
 * @param initializer A lambda with [SubscriptionPreviewClient.Builder] receiver that initializes preview states
 * @return A subscription client that can be used to provide mock data for subscriptions in Compose previews
 */
fun SubscriptionPreviewClient(initializer: SubscriptionPreviewClient.Builder.() -> Unit): SubscriptionPreviewClient {
    return SubscriptionPreviewClient.Builder().apply(initializer).build()
}
