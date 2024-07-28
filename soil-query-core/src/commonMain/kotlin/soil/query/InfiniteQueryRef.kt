// Copyright 2024 Soil Contributors
// SPDX-License-Identifier: Apache-2.0

package soil.query

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch

/**
 * A reference to an [Query] for [InfiniteQueryKey].
 *
 * @param T Type of data to retrieve.
 * @param S Type of parameter.
 * @property key Instance of a class implementing [InfiniteQueryKey].
 * @param query Transparently referenced [Query].
 * @constructor Creates an [InfiniteQueryRef].
 */
class InfiniteQueryRef<T, S>(
    val key: InfiniteQueryKey<T, S>,
    val options: QueryOptions,
    query: Query<QueryChunks<T, S>>
) : Query<QueryChunks<T, S>> by query {

    /**
     * Starts the [Query].
     *
     * This function must be invoked when a new mount point (subscriber) is added.
     */
    suspend fun start() {
        command.send(InfiniteQueryCommands.Connect(key))
        event.collect(::handleEvent)
    }

    /**
     * Invalidates the [Query].
     *
     * Calling this function will invalidate the retrieved data of the [Query],
     * setting [QueryModel.isInvalidated] to `true` until revalidation is completed.
     */
    suspend fun invalidate() {
        command.send(InfiniteQueryCommands.Invalidate(key, state.value.revision))
    }

    /**
     * Resumes the [Query].
     */
    private suspend fun resume() {
        command.send(InfiniteQueryCommands.Connect(key, state.value.revision))
    }

    /**
     * Fetches data for the [InfiniteQueryKey] using the value of [param].
     */
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
