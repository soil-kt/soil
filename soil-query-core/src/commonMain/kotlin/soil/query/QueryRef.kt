// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class QueryRef<T>(
    val key: QueryKey<T>,
    query: Query<T>
) : Query<T> by query {
    fun start(scope: CoroutineScope) {
        actor.launchIn(scope = scope)
        scope.launch {
            command.send(QueryCommands.Connect(key))
            event.collect(::handleEvent)
        }
    }

    suspend fun invalidate() {
        command.send(QueryCommands.Invalidate(key, state.value.revision))
    }

    private suspend fun resume() {
        command.send(QueryCommands.Connect(key, state.value.revision))
    }

    private suspend fun handleEvent(e: QueryEvent) {
        when (e) {
            QueryEvent.Invalidate -> invalidate()
            QueryEvent.Resume -> resume()
            QueryEvent.Ping -> Unit
        }
    }
}
