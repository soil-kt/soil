// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import soil.query.core.Marker

internal class SwrMutation<T, S>(
    override val key: MutationKey<T, S>,
    override val marker: Marker,
    private val mutation: Mutation<T>
) : MutationRef<T, S> {

    override val options: MutationOptions
        get() = mutation.options

    override val state: StateFlow<MutationState<T>>
        get() = mutation.state

    override fun launchIn(scope: CoroutineScope): Job {
        return mutation.launchIn(scope)
    }

    override suspend fun send(command: MutationCommand<T>) {
        mutation.command.send(command)
    }
}
