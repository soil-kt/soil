// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first

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
        mutateAsync(variable)
        val submittedAt = state.value.submittedAt
        val result = state.dropWhile { it.submittedAt <= submittedAt }.first()
        if (result.isSuccess) {
            return result.data!!
        } else if (result.isFailure) {
            throw result.error!!
        } else {
            error("Unexpected ${result.status}")
        }
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
