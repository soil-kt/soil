// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.StateFlow
import soil.query.core.Actor
import soil.query.core.Marker


/**
 * A reference to a Mutation for [MutationKey].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
interface MutationRef<T, S> : Actor {

    /**
     * A unique identifier used for managing [MutationKey].
     */
    val id: MutationId<T, S>

    /**
     * [State Flow][StateFlow] to receive the current state of the mutation.
     */
    val state: StateFlow<MutationState<T>>

    /**
     * Mutates the variable.
     *
     * @param variable The variable to be mutated.
     * @return The result of the mutation.
     */
    suspend fun mutate(variable: S): T

    /**
     * Mutates the variable asynchronously.
     *
     * @param variable The variable to be mutated.
     */
    suspend fun mutateAsync(variable: S)

    /**
     * Resets the mutation state.
     */
    suspend fun reset()
}

/**
 * Creates a new [MutationRef] instance.
 *
 * @param key The [MutationKey] for the Mutation.
 * @param marker The Marker specified in [MutationClient.getMutation].
 * @param mutation The Mutation to create a reference.
 */
fun <T, S> MutationRef(
    key: MutationKey<T, S>,
    marker: Marker,
    mutation: Mutation<T>
): MutationRef<T, S> {
    return MutationRefImpl(key, marker, mutation)
}

private class MutationRefImpl<T, S>(
    private val key: MutationKey<T, S>,
    private val marker: Marker,
    private val mutation: Mutation<T>
) : MutationRef<T, S> {

    override val id: MutationId<T, S>
        get() = key.id

    override val state: StateFlow<MutationState<T>>
        get() = mutation.state

    override fun launchIn(scope: CoroutineScope): Job {
        return mutation.launchIn(scope)
    }

    override suspend fun mutate(variable: S): T {
        val deferred = CompletableDeferred<T>()
        send(MutationCommands.Mutate(key, variable, state.value.revision, marker, deferred::completeWith))
        return deferred.await()
    }

    override suspend fun mutateAsync(variable: S) {
        send(MutationCommands.Mutate(key, variable, state.value.revision, marker))
    }

    override suspend fun reset() {
        send(MutationCommands.Reset())
    }

    private suspend fun send(command: MutationCommand<T>) {
        mutation.command.send(command)
    }
}
