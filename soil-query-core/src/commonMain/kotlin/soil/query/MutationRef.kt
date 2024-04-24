// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class MutationRef<T, S>(
    val key: MutationKey<T, S>,
    mutation: Mutation<T>
) : Mutation<T> by mutation {

    fun start(scope: CoroutineScope) {
        actor.launchIn(scope = scope)
        scope.launch {
            event.collect(::handleEvent)
        }
    }

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

    suspend fun mutateAsync(variable: S) {
        command.send(MutationCommands.Mutate(key, variable, state.value.revision))
    }

    suspend fun reset() {
        command.send(MutationCommands.Reset())
    }

    private fun handleEvent(e: MutationEvent) {
        when (e) {
            MutationEvent.Ping -> Unit
        }
    }
}
