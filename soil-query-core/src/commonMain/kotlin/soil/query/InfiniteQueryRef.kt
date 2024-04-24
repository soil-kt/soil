// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

class InfiniteQueryRef<T, S>(
    val key: InfiniteQueryKey<T, S>,
    query: Query<QueryChunks<T, S>>
) : Query<QueryChunks<T, S>> by query {

    fun start(scope: CoroutineScope) {
        actor.launchIn(scope = scope)
        scope.launch {
            command.send(InfiniteQueryCommands.Connect(key))
            event.collect(::handleEvent)
        }
    }

    suspend fun invalidate() {
        command.send(InfiniteQueryCommands.Invalidate(key, state.value.revision))
    }

    private suspend fun resume() {
        command.send(InfiniteQueryCommands.Connect(key, state.value.revision))
    }

    suspend fun loadMore(param: S) {
        command.send(InfiniteQueryCommands.LoadMore(key, param))
    }

    private suspend fun handleEvent(e: QueryEvent) {
        when (e) {
            QueryEvent.Invalidate -> invalidate()
            QueryEvent.Resume -> resume()
            QueryEvent.Ping -> Unit
        }
    }
}
