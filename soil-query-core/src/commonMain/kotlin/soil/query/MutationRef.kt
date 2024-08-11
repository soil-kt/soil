// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.completeWith
import kotlinx.coroutines.flow.StateFlow
import soil.query.core.Actor

/**
 * A reference to a Mutation for [MutationKey].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 */
interface MutationRef<T, S> : Actor {

    val key: MutationKey<T, S>
    val options: MutationOptions
    val state: StateFlow<MutationState<T>>

    /**
     * Sends a [MutationCommand] to the Actor.
     */
    suspend fun send(command: MutationCommand<T>)
}

/**
 * Mutates the variable.
 *
 * @param variable The variable to be mutated.
 * @return The result of the mutation.
 */
suspend fun <T, S> MutationRef<T, S>.mutate(variable: S): T {
    val deferred = CompletableDeferred<T>()
    send(MutationCommands.Mutate(key, variable, state.value.revision, deferred::completeWith))
    return deferred.await()
}

/**
 * Mutates the variable asynchronously.
 *
 * @param variable The variable to be mutated.
 */
suspend fun <T, S> MutationRef<T, S>.mutateAsync(variable: S) {
    send(MutationCommands.Mutate(key, variable, state.value.revision))
}

/**
 * Resets the mutation state.
 */
suspend fun <T, S> MutationRef<T, S>.reset() {
    send(MutationCommands.Reset())
}
