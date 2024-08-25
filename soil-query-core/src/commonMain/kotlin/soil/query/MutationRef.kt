// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CompletableDeferred
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

    val key: MutationKey<T, S>
    val marker: Marker
    val state: StateFlow<MutationState<T>>

    /**
     * Sends a [MutationCommand] to the Actor.
     */
    suspend fun send(command: MutationCommand<T>)

    /**
     * Mutates the variable.
     *
     * @param variable The variable to be mutated.
     * @return The result of the mutation.
     */
    suspend fun mutate(variable: S): T {
        val deferred = CompletableDeferred<T>()
        send(MutationCommands.Mutate(key, variable, state.value.revision, marker, deferred::completeWith))
        return deferred.await()
    }

    /**
     * Mutates the variable asynchronously.
     *
     * @param variable The variable to be mutated.
     */
    suspend fun mutateAsync(variable: S) {
        send(MutationCommands.Mutate(key, variable, state.value.revision, marker))
    }

    /**
     * Resets the mutation state.
     */
    suspend fun reset() {
        send(MutationCommands.Reset())
    }
}
