// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.tooling

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import soil.query.MutationClient
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.MutationReceiver
import soil.query.MutationRef
import soil.query.MutationState
import soil.query.MutationTestTag
import soil.query.core.Marker
import soil.query.core.TestTag
import soil.query.core.UniqueId
import soil.query.core.getOrThrow
import soil.query.marker.TestTagMarker

/**
 * Usage:
 * ```kotlin
 * val mutationClient = MutationPreviewClient {
 *     on(MyMutationId1) { MutationState.success("data") }
 *     on(MyMutationId2) { .. }
 * }
 * ```
 */
@Stable
class MutationPreviewClient(
    private val previewData: Map<UniqueId, MutationState<*>>,
    private val previewDataByTag: Map<TestTag, MutationState<*>>
) : MutationClient {

    override val mutationReceiver: MutationReceiver = MutationReceiver

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getMutation(
        key: MutationKey<T, S>,
        marker: Marker
    ): MutationRef<T, S> {
        val state = previewData[key.id] as? MutationState<T>
            ?: marker[TestTagMarker.Key]?.value?.let { previewDataByTag[it] as? MutationState<T> }
            ?: MutationState.initial()
        return SnapshotMutation(key.id, MutableStateFlow(state))
    }

    private class SnapshotMutation<T, S>(
        override val id: MutationId<T, S>,
        override val state: StateFlow<MutationState<T>>
    ) : MutationRef<T, S> {
        override fun close() = Unit
        override suspend fun reset() = Unit
        override suspend fun mutate(variable: S): T = state.value.reply.getOrThrow()
        override suspend fun mutateAsync(variable: S) = Unit
    }

    /**
     * Builder for [MutationPreviewClient].
     */
    class Builder {
        private val previewData = mutableMapOf<UniqueId, MutationState<*>>()
        private val previewDataByTag = mutableMapOf<TestTag, MutationState<*>>()

        /**
         * Registers a preview state for the mutation with the specified ID.
         *
         * @param id The mutation ID that identifies this mutation
         * @param snapshot A function that provides the mutation state to be returned for this ID
         */
        fun <T, S> on(id: MutationId<T, S>, snapshot: () -> MutationState<T>) {
            previewData[id] = snapshot()
        }

        /**
         * Registers a preview state for the mutation with the specified test tag.
         *
         * @param testTag The test tag that identifies this mutation
         * @param snapshot A function that provides the mutation state to be returned for this tag
         */
        fun <T, S> on(testTag: MutationTestTag<T, S>, snapshot: () -> MutationState<T>) {
            previewDataByTag[testTag] = snapshot()
        }

        /**
         * Builds a new instance of [MutationPreviewClient] with the registered preview states.
         *
         * @return A new [MutationPreviewClient] instance
         */
        fun build(): MutationPreviewClient = MutationPreviewClient(previewData, previewDataByTag)
    }
}

/**
 * Creates a [MutationPreviewClient] with the provided [initializer].
 *
 * @param initializer A lambda with [MutationPreviewClient.Builder] receiver that initializes preview states
 * @return A mutation client that can be used to provide mock data for mutations in Compose previews
 */
fun MutationPreviewClient(initializer: MutationPreviewClient.Builder.() -> Unit): MutationPreviewClient {
    return MutationPreviewClient.Builder().apply(initializer).build()
}
