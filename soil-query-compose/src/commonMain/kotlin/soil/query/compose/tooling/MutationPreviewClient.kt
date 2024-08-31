// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.tooling

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import soil.query.MutationClient
import soil.query.MutationCommand
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.MutationOptions
import soil.query.MutationRef
import soil.query.MutationState
import soil.query.core.Marker
import soil.query.core.UniqueId

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
    override val defaultMutationOptions: MutationOptions = MutationOptions
) : MutationClient {

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getMutation(
        key: MutationKey<T, S>,
        marker: Marker
    ): MutationRef<T, S> {
        val state = previewData[key.id] as? MutationState<T> ?: MutationState.initial()
        val options = key.onConfigureOptions()?.invoke(defaultMutationOptions) ?: defaultMutationOptions
        return SnapshotMutation(key, options, marker, MutableStateFlow(state))
    }

    private class SnapshotMutation<T, S>(
        override val key: MutationKey<T, S>,
        override val options: MutationOptions,
        override val marker: Marker,
        override val state: StateFlow<MutationState<T>>
    ) : MutationRef<T, S> {
        override fun launchIn(scope: CoroutineScope): Job = Job()
        override suspend fun send(command: MutationCommand<T>) = Unit
    }

    /**
     * Builder for [MutationPreviewClient].
     */
    class Builder {
        private val previewData = mutableMapOf<UniqueId, MutationState<*>>()

        fun <T, S> on(id: MutationId<T, S>, snapshot: () -> MutationState<T>) {
            previewData[id] = snapshot()
        }

        fun build(): MutationPreviewClient = MutationPreviewClient(previewData)
    }
}

/**
 * Creates a [MutationPreviewClient] with the provided [initializer].
 */
fun MutationPreviewClient(initializer: MutationPreviewClient.Builder.() -> Unit): MutationPreviewClient {
    return MutationPreviewClient.Builder().apply(initializer).build()
}
