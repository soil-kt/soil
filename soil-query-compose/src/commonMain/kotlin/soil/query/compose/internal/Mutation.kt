// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query.compose.internal

import androidx.compose.runtime.RememberObserver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import soil.query.MutationClient
import soil.query.MutationId
import soil.query.MutationKey
import soil.query.MutationRef
import soil.query.MutationState
import soil.query.compose.MutationConfig

internal fun <T, S> newMutation(
    key: MutationKey<T, S>,
    config: MutationConfig,
    client: MutationClient,
    scope: CoroutineScope
): MutationRef<T, S> = Mutation(key, config, client, scope)

private class Mutation<T, S>(
    key: MutationKey<T, S>,
    config: MutationConfig,
    client: MutationClient,
    private val scope: CoroutineScope
) : MutationRef<T, S>, RememberObserver {

    private val mutation: MutationRef<T, S> = client.getMutation(key, config.marker)
    private val optimize: (MutationState<T>) -> MutationState<T> = config.optimizer::omit

    override val id: MutationId<T, S> = mutation.id

    private val _state: MutableStateFlow<MutationState<T>> = MutableStateFlow(
        value = optimize(mutation.state.value)
    )

    override val state: StateFlow<MutationState<T>> = _state

    override suspend fun mutate(variable: S): T = mutation.mutate(variable)

    override suspend fun mutateAsync(variable: S) = mutation.mutateAsync(variable)

    override suspend fun reset() = mutation.reset()

    override fun launchIn(scope: CoroutineScope): Job {
        return scope.launch {
            mutation.state.collect { _state.value = optimize(it) }
        }
    }

    // ----- RememberObserver -----//
    private var jobs: List<Job>? = null

    override fun onAbandoned() = stop()

    override fun onForgotten() = stop()

    override fun onRemembered() {
        stop()
        start()
    }

    private fun start() {
        val job1 = mutation.launchIn(scope)
        val job2 = launchIn(scope)
        jobs = listOf(job1, job2)
    }

    private fun stop() {
        jobs?.forEach { it.cancel() }
        jobs = null
    }
}
