// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CompletableDeferred
import soil.query.internal.toResultCallback

/**
 * A reference to a [Mutation] for [MutationKey].
 *
 * @param T Type of the return value from the mutation.
 * @param S Type of the variable to be mutated.
 * @property key Instance of a class implementing [MutationKey].
 * @param mutation The mutation to perform.
 * @constructor Creates a [MutationRef].
 */
class MutationRef<T, S>(
    val key: MutationKey<T, S>,
    val options: MutationOptions,
    mutation: Mutation<T>
) : Mutation<T> by mutation {

    /**
     * Mutates the variable.
     *
     * @param variable The variable to be mutated.
     * @return The result of the mutation.
     */
    suspend fun mutate(variable: S): T {
        val deferred = CompletableDeferred<T>()
        command.send(MutationCommands.Mutate(key, variable, state.value.revision, deferred.toResultCallback()))
        return deferred.await()
    }

    /**
     * Mutates the variable asynchronously.
     *
     * @param variable The variable to be mutated.
     */
    suspend fun mutateAsync(variable: S) {
        command.send(MutationCommands.Mutate(key, variable, state.value.revision))
    }

    /**
     * Resets the mutation state.
     */
    suspend fun reset() {
        command.send(MutationCommands.Reset())
    }
}
