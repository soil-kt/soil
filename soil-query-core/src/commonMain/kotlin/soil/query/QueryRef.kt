// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

/**
 * A reference to an [Query] for [QueryKey].
 *
 * @param T Type of data to retrieve.
 * @property key Instance of a class implementing [QueryKey].
 * @param query Transparently referenced [Query].
 * @constructor Creates an [QueryRef]
 */
class QueryRef<T>(
    val key: QueryKey<T>,
    val options: QueryOptions,
    query: Query<T>
) : Query<T> by query {

    /**
     * Starts the [Query].
     *
     * This function must be invoked when a new mount point (subscriber) is added.
     *
     * @param scope The [CoroutineScope] to launch the [Query] actor.
     */
    fun start(scope: CoroutineScope) {
        actor.launchIn(scope = scope)
        scope.launch {
            command.send(QueryCommands.Connect(key))
            event.collect(::handleEvent)
        }
    }

    /**
     * Invalidates the [Query].
     *
     * Calling this function will invalidate the retrieved data of the [Query],
     * setting [QueryModel.isInvalidated] to `true` until revalidation is completed.
     */
    suspend fun invalidate() {
        command.send(QueryCommands.Invalidate(key, state.value.revision))
    }

    /**
     * Resumes the [Query].
     */
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
