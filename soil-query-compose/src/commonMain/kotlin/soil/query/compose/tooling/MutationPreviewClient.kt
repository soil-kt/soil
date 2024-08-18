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
import soil.query.MutationKey
import soil.query.MutationOptions
import soil.query.MutationRef
import soil.query.MutationState
import soil.query.core.UniqueId

/**
 * Usage:
 * ```kotlin
 * val client = MutationPreviewClient(
 *   previewData = mapOf(
 *      MyMutationId to MutationState.success("data"),
 *      ..
 *   )
 * )
 * ```
 */
@Stable
class MutationPreviewClient(
    private val previewData: Map<UniqueId, MutationState<*>>,
    override val defaultMutationOptions: MutationOptions = MutationOptions
) : MutationClient {

    @Suppress("UNCHECKED_CAST")
    override fun <T, S> getMutation(key: MutationKey<T, S>): MutationRef<T, S> {
        val state = previewData[key.id] as? MutationState<T> ?: MutationState.initial()
        val options = key.onConfigureOptions()?.invoke(defaultMutationOptions) ?: defaultMutationOptions
        return SnapshotMutation(key, options, MutableStateFlow(state))
    }

    private class SnapshotMutation<T, S>(
        override val key: MutationKey<T, S>,
        override val options: MutationOptions,
        override val state: StateFlow<MutationState<T>>
    ) : MutationRef<T, S> {
        override fun launchIn(scope: CoroutineScope): Job = Job()
        override suspend fun send(command: MutationCommand<T>) = Unit
    }
}
